package at.jku.dke.pm.services.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.services.EventRepository;

public class JdbcEventRepository implements EventRepository {

	protected static final Logger logger = LoggerFactory.getLogger(JdbcEventRepository.class);

	protected final JdbcTemplate template;

	protected static final String SQL_INSERT_EVENT_DATA = "insert into RAW_EVENTS_DATA (EVENT_ID, KEY, VALUE) VALUES (?, ?, ?)";

	public JdbcEventRepository(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Event> saveAll(List<Event> events) {

		for (Event e : events) {
			GeneratedKeyHolder holder = new GeneratedKeyHolder();

			template.update(new RawEventPSC(e), holder);
			e.setId(holder.getKey().intValue());

			logger.debug("Store Event: {} {}", holder.getKeys(), e);

			if (!e.getAttributes().isEmpty()) {
				logger.debug("      Data : {}",
						template.batchUpdate(SQL_INSERT_EVENT_DATA, new BatchEventDataSetter(e)).length);
			}
		}

		return events;
	}

	public static class RawEventPSC implements PreparedStatementCreator {

		protected final String SQL_INSERT_EVENT = "insert into RAW_EVENTS (SOURCE, EVENT_TYPE, EVENT_TS) VALUES (?, ?, ?)";

		protected final Event event;

		public RawEventPSC(Event event) {
			this.event = event;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			PreparedStatement ps = connection.prepareStatement(SQL_INSERT_EVENT, Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, event.getSource());
			ps.setString(2, event.getType());
			ps.setTimestamp(3, Timestamp.valueOf(event.getEventTs()));

			return ps;
		}

	};

	public static class BatchEventDataSetter implements BatchPreparedStatementSetter {

		protected final Event event;

		protected final List<String> keys;

		public BatchEventDataSetter(Event event) {
			this.event = event;
			this.keys = new ArrayList<String>(event.getAttributes().keySet());
		}

		@Override
		public void setValues(PreparedStatement ps, int i) throws SQLException {
			ps.setInt(1, event.getId());

			String key = keys.get(i);
			ps.setString(2, key);
			ps.setString(3, String.valueOf(event.getAttributes().get(key)));

		}

		@Override
		public int getBatchSize() {
			return keys.size();
		}

	}

}
