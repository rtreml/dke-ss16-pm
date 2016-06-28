package at.jku.dke.pm.services.repositories;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.services.CaseRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JdbcCaseRepository implements CaseRepository {

	protected static final Logger logger = LoggerFactory.getLogger(JdbcCaseRepository.class);

	protected final JdbcTemplate template;

	protected final static String SQL_DELETE_CASE = "delete from CASES where ID = ?";

	protected final static String SQL_INSERT_EVENTS = "insert into EVENTS (ID, CASE_ID, EVENT_TYPE, EVENT_TS) VALUES (?, ?, ?, ?)";

	protected final static String SQL_INSERT_EVENT_DATA = "insert into EVENTS_DATA (EVENT_ID, CASE_ID, KEY, VALUE) VALUES (?, ?, ?, ?)";

	public JdbcCaseRepository(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	@Override
	public Case save(Case c) {

		if (c.getId() != null) {
			template.update(SQL_DELETE_CASE, c.getId());
		}

		GeneratedKeyHolder holder = new GeneratedKeyHolder();

		template.update(new CasePSC(c), holder);
		c.setId(holder.getKey().intValue());

		logger.debug("Store Case: {} {}", holder.getKeys(), c);

		// Events speichern
		if (c.getEvents() != null && !c.getEvents().isEmpty()) {
			for (Event e : c.getEvents()) {
				template.update(SQL_INSERT_EVENTS, e.getId(), c.getId(), e.getType(), Timestamp.valueOf(e.getEventTs()));

				if (!e.getAttributes().isEmpty()) {
					template.batchUpdate(SQL_INSERT_EVENT_DATA, new BatchPreparedStatementSetter() {
						protected List<String> keys = null;

						@Override
						public void setValues(PreparedStatement ps, int i) throws SQLException {

							if (keys == null) {
								keys = new ArrayList<String>(e.getAttributes().keySet());
							}

							ps.setInt(1, e.getId());
							ps.setInt(2, c.getId());

							String key = keys.get(i);
							ps.setString(3, key);
							ps.setString(4, String.valueOf(e.getAttributes().get(key)));

						}

						@Override
						public int getBatchSize() {
							return e.getAttributes().size();
					}
				});
				}

			}
		}

		return c;
	}

	protected final static String SQL_LOAD_CASE = "select * from CASES where ID = ?";

	protected final static String SQL_LOAD_CASE_EVENTS = "select * from EVENTS where CASE_ID = ? order by EVENT_TS, EVENT_TYPE";

	@Override
	public Case findById(int caseId) {
		Case c = template.query(SQL_LOAD_CASE, caseRowMapper, caseId).stream().findFirst().orElse(null);

		if (c != null) {
			c.setEvents(template.query(SQL_LOAD_CASE_EVENTS, EventRowMapper.caseEvent(template, c.getId()), c.getId()));
		}

		return c;
	}

	protected final static String SQL_FIND_CASES_BY_FOOTPRINT = "select * from CASES where PROCESS_ID = ? and FOOTPRINT = ?";

	@Override
	public List<Case> findByFootprint(String processId, String footprint) {
		List<Case> cases = template.query(SQL_FIND_CASES_BY_FOOTPRINT, caseRowMapper, processId, footprint);

		cases.forEach(c -> c.setEvents(template.query(SQL_LOAD_CASE_EVENTS,
				EventRowMapper.caseEvent(template, c.getId()), c.getId())));

		return cases;
	};

	protected final RowMapper<Case> caseRowMapper = new RowMapper<Case>() {

		protected ObjectMapper mapper = new ObjectMapper();

		@Override
		public Case mapRow(ResultSet rs, int rowNum) throws SQLException {
			Case c = new Case();

			c.setId(rs.getInt("ID"));
			c.setProcessId(rs.getString("PROCESS_ID"));
			c.setName(rs.getString("NAME"));
			c.setEventId(rs.getInt("EVENT_ID"));

			Timestamp ts = rs.getTimestamp("START_TS");
			c.setStartTs(ts != null ? ts.toLocalDateTime() : null);
			ts = rs.getTimestamp("END_TS");
			c.setEndTs(ts != null ? ts.toLocalDateTime() : null);
			c.setDuration(Duration.ofSeconds(rs.getLong("DURATION")));
			c.setFootprint(rs.getString("FOOTPRINT"));

			String identStr = rs.getString("IDENTIFIER");
			Map<String, Object> ident = Collections.emptyMap();
			try {
				if (identStr != null) {
					ident = mapper.readValue(identStr, new TypeReference<Map<String, Object>>() {
					});
				}
			} catch (IOException e) {
				logger.error("JSON Error", e);
			}
			c.setIdentifier(ident);
			return c;
		}

	};

	public static class CasePSC implements PreparedStatementCreator {

		protected ObjectMapper mapper = new ObjectMapper();

		protected final String SQL_INSERT_CASE = "insert into CASES (PROCESS_ID, NAME, EVENT_ID, IDENTIFIER, START_TS, END_TS, DURATION, FOOTPRINT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		protected final Case c;

		public CasePSC(Case c) {
			this.c = c;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			PreparedStatement ps = connection.prepareStatement(SQL_INSERT_CASE, Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, c.getProcessId());
			ps.setString(2, c.getName());
			ps.setInt(3, c.getEventId());

			String ident = null;
			try {
				ident = mapper.writeValueAsString(c.getIdentifier());
			} catch (JsonProcessingException e) {
				logger.error("JSON Error", e);
			}

			ps.setString(4, ident);
			ps.setTimestamp(5, Timestamp.valueOf(c.getStartTs()));
			ps.setTimestamp(6, Timestamp.valueOf(c.getEndTs()));
			ps.setLong(7, c.getDuration().getSeconds());
			ps.setString(8, c.getFootprint());

			return ps;
		}

	}

}
