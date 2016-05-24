package at.jku.dke.pm.services.repositories;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.EventType;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.domain.ProcessData;
import at.jku.dke.pm.domain.ProcessNet;
import at.jku.dke.pm.services.ProcessRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JdbcProcessRepository implements ProcessRepository {

	protected static final Logger logger = LoggerFactory.getLogger(JdbcProcessRepository.class);

	protected final JdbcTemplate template;

	public JdbcProcessRepository(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	protected final static String SQL_LOAD_ALL_PROCESSINFO = "select * from PROCESS order by NAME";

	protected final static String SQL_LOAD_PROCESSINFO = "select * from PROCESS where ID = ?";

	protected final static String SQL_LOAD_EVENTTYPES = "select * from MD_EVENTS where PROCESS_ID = ? order by ID";

//	protected final static String SQL_LOAD_ALL_MODEL = "select FOOTPRINT, count(*) as NO_CASE from CASES where PROCESS_ID = ? group by FOOTPRINT order by 2 DESC";
	protected final static String SQL_LOAD_ALL_MODEL = "select * from MODEL where PROCESS_ID = ? order by NO_INSTANCES DESC";

//	protected final static String SQL_LOAD_MODEL = "select FOOTPRINT, count(*) as NO_CASE from CASES where PROCESS_ID = ? and FOOTPRINT = ? group by FOOTPRINT";
	protected final static String SQL_LOAD_MODEL = "select * from MODEL where PROCESS_ID = ? and FOOTPRINT = ?";

	protected final static String SQL_LOAD_MODEL_CASES = "select * from CASES where PROCESS_ID = ? and FOOTPRINT = ?";

	@Override
	public List<ProcessData> findAll() {
		return template.query(SQL_LOAD_ALL_PROCESSINFO, processMapper);
	}

	@Override
	public ProcessData findById(String processId) {
		ProcessData process = template.query(SQL_LOAD_PROCESSINFO, processMapper, processId).stream().findFirst()
				.orElse(null);

		if (process != null) {
			process.setModels(findAllModels(processId));
			process.setEventTypes(template.query(SQL_LOAD_EVENTTYPES, new RowMapper<EventType>() {

				@Override
				public EventType mapRow(ResultSet rs, int rowNum) throws SQLException {
					EventType e = new EventType();

					e.setId(rs.getString("ID"));
					e.setName(rs.getString("NAME"));

					return e;
				}

			}, processId));
		}

		return process;
	}

	@Override
	public Model findModelById(String processId, String id) {
		Model m = template.query(SQL_LOAD_MODEL, modelMapper, processId, id).stream().findFirst().orElse(null);

		if (m != null) {
			m.setCases(template.query(SQL_LOAD_MODEL_CASES, new RowMapper<Case>() {

				protected ObjectMapper mapper = new ObjectMapper();

				@Override
				public Case mapRow(ResultSet rs, int rowNum) throws SQLException {
					Case c = new Case();

					c.setId(rs.getInt("ID"));
					c.setProcessId(rs.getString("PROCESS_ID"));
					c.setName(rs.getString("NAME"));
					c.setEventId(rs.getInt("EVENT_ID"));

					String identStr = rs.getString("IDENTIFIER");
					Map<String, Object> ident = Collections.emptyMap();
					try {
						ident = mapper.readValue(identStr, new TypeReference<Map<String, Object>>() {
						});
					} catch (IOException e) {
						logger.error("JSON Error", e);
					}
					c.setIdentifier(ident);
					return c;
				}

			}, processId, id));
		}

		return m;
	}

	@Override
	public List<Model> findAllModels(String processId) {
		List<Model> m = template.query(SQL_LOAD_ALL_MODEL, modelMapper, processId);
		return m;
	}

	protected final static RowMapper<ProcessData> processMapper = new RowMapper<ProcessData>() {

		@Override
		public ProcessData mapRow(ResultSet rs, int rowNum) throws SQLException {
			ProcessData i = new ProcessData();

			i.setId(rs.getString("ID"));
			i.setName(rs.getString("NAME"));

			return i;
		}

	};

	protected final static RowMapper<Model> modelMapper = new RowMapper<Model>() {

		// PROCESS_ID, FOOTPRINT, NAME, NO_INSTANCES, MIN_DURATION, MAX_DURATION, AVG_DURATION, GRAPH
		protected ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
				false).registerModule(new JavaTimeModule());

		@Override
		public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
			Model m = new Model();

			m.setProcessId(rs.getString("PROCESS_ID"));
			m.setFootprint(rs.getString("FOOTPRINT"));
			// m.setName(rs.getString("NAME"));
			m.setNoCases(rs.getInt("NO_INSTANCES"));
			m.setMinDuration(Duration.ofSeconds(rs.getLong("MIN_DURATION")));
			m.setMaxDuration(Duration.ofSeconds(rs.getLong("MAX_DURATION")));
			m.setAvgDuration(Duration.ofSeconds(rs.getLong("AVG_DURATION")));

			String processNet = rs.getString("PROCESS_NET");
			ProcessNet net = null;
			try {
				net = mapper.readValue(processNet, ProcessNet.class);
			} catch (IOException e) {
				logger.error("JSON Error", e);
			}
			m.setProcessNet(net);

			return m;
		}

	};

}
