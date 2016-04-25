package at.jku.dke.pm.services.repositories;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.domain.ProcessInfo;
import at.jku.dke.pm.services.ModelRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JdbcModelRepository implements ModelRepository {

	protected static final Logger logger = LoggerFactory.getLogger(JdbcModelRepository.class);

	protected final JdbcTemplate template;

	public JdbcModelRepository(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	protected final static String SQL_LOAD_PROCESSINFO = "select * from PROCESS where ID = ?";

	protected final static String SQL_LOAD_ALL_MODEL = "select FOOTPRINT, count(*) as NO_CASE from CASES where PROCESS_ID = ? group by FOOTPRINT order by 2 DESC";

	protected final static String SQL_LOAD_MODEL = "select FOOTPRINT, count(*) as NO_CASE from CASES where PROCESS_ID = ? and FOOTPRINT = ? group by FOOTPRINT";

	protected final static String SQL_LOAD_MODEL_CASES = "select * from CASES where PROCESS_ID = ? and FOOTPRINT = ?";

	@Override
	public ProcessInfo findProcessById(String processId) {
		return template.query(SQL_LOAD_PROCESSINFO, new RowMapper<ProcessInfo>() {

			@Override
			public ProcessInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ProcessInfo i = new ProcessInfo();
				
				i.setId(rs.getString("ID"));
				i.setName(rs.getString("NAME"));
				
				return i;
			}
			
		}, processId).stream().findFirst().orElse(null);
	}
	@Override
	public Model findById(String processId, String id) {
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
	public List<Model> findAll(String processId) {
		List<Model> m = template.query(SQL_LOAD_ALL_MODEL, modelMapper, processId);
		return m;
	}

	protected final static RowMapper<Model> modelMapper = new RowMapper<Model>() {

		@Override
		public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
			Model m = new Model();

			m.setFootprint(rs.getString("FOOTPRINT"));
			m.setNoCases(rs.getInt("NO_CASE"));
			// c.setId(rs.getInt("ID"));
			// c.setProcessId(rs.getString("PROCESS_ID"));
			// c.setName(rs.getString("NAME"));
			// c.setEventId(rs.getInt("EVENT_ID"));

			// String identStr = rs.getString("IDENTIFIER");
			// Map<String, Object> ident = Collections.emptyMap();
			// try {
			// ident = mapper.readValue(identStr, new TypeReference<Map<String, Object>>() {
			// });
			// } catch (IOException e) {
			// logger.error("JSON Error", e);
			// }
			// c.setIdentifier(ident);
			return m;
		}

	};


}
