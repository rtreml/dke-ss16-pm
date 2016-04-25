package at.jku.dke.pm.services.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import at.jku.dke.pm.domain.Event;

public class EventRowMapper implements RowMapper<Event> {

	protected enum MySource {
		RAW, CASE;
	}
	
	protected final MySource source;
	
	protected final String sqlData;

	protected static final ResultSetExtractor<Map<String, Object>> dataExtractor = new ResultSetExtractor<Map<String, Object>>() {

		@Override
		public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
			Map<String, Object> m = new HashMap<>();

			while (rs.next()) {
				m.put(rs.getString("KEY"), rs.getString("VALUE"));
			}

			return m;
		}

	};

	protected final JdbcTemplate template;

	/*
	 * private constructor
	 */
	private EventRowMapper(JdbcTemplate template, MySource source, String sqlData) {
		this.template = template;
		this.source = source;
		this.sqlData = sqlData;
		
	}

	/*
	 * Factory Methods
	 */
	public static EventRowMapper rawEvent(JdbcTemplate template) {
		return new EventRowMapper(template, MySource.RAW, "select * from RAW_EVENTS_DATA where EVENT_ID = ?");
	}

	public static EventRowMapper caseEvent(JdbcTemplate template, int caseId) {
		return new EventRowMapper(template, MySource.CASE, "select * from EVENTS_DATA where EVENT_ID = ? and CASE_ID = " + caseId);
	}

	@Override
	public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
		Event e = new Event();

		e.setId(rs.getInt("ID"));
		e.setEventTs(rs.getTimestamp("EVENT_TS").toLocalDateTime());
		e.setType(rs.getString("EVENT_TYPE"));

		if (source == MySource.RAW) {
			e.setSource(rs.getString("SOURCE"));
			e.setLegacyId(rs.getString("LEGACY_ID"));
		}
		// DATA
		e.setAttributes(template.query(sqlData, dataExtractor, e.getId()));

		return e;
	}

}