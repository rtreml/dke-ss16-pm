package at.jku.dke.pm.collect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import at.jku.dke.pm.collect.footprint.FootprintGenerator;
import at.jku.dke.pm.collect.footprint.SumUpFootprintGenerator;
import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;
import at.jku.dke.pm.services.repositories.EventRowMapper;

/**
 * Alle cases identifizieren welche mit einer Bestellposition zusammen hängen.
 * Start @see {@link Events#BESTELLPOSITION_ERSTELLT}
 * 
 * @author tremlro
 *
 */
public class BestellPosCaseCollector implements CaseCollector {

	protected static final Logger logger = LoggerFactory.getLogger(BestellPosCaseCollector.class);

	public static final String PROCESS_ID = "DKE_BESTELL_POS";

	protected JdbcTemplate template;

	protected RowMapper<Event> eventRowMapper;
	
	protected FootprintGenerator footprintGenerator = new SumUpFootprintGenerator();

	public BestellPosCaseCollector(DataSource dataSource) {
		template = new JdbcTemplate(dataSource);
		eventRowMapper = EventRowMapper.rawEvent(template);
	}

	protected static final String SQL_EVENT_BY_TYPE = "select * from RAW_EVENTS where EVENT_TYPE = ?";

	@Override
	public List<Case> identifyCases() {

		List<Case> cases = template.query(SQL_EVENT_BY_TYPE, new RowMapper<Case>() {

			@Override
			public Case mapRow(ResultSet rs, int rowNum) throws SQLException {

				Event e = eventRowMapper.mapRow(rs, rowNum);

				Case c = new Case();

				c.setProcessId(PROCESS_ID);
				c.setEventId(e.getId());
				c.setName(String.format("Pos %s/%s", e.getAttributes().get(EventAttributes.ID_BESTELLUNG), e
						.getAttributes().get(EventAttributes.ID_BESTELL_POS)));
				c.addIdentifiers(e.getAttributes());

				return c;
			}

		}, Events.BESTELLPOSITION_ERSTELLT);

		logger.debug("found {} cases", cases.size());

		return cases;
	}

	@Override
	public List<Event> collectCaseEvents(int caseId) {
		throw new NotImplementedException("not Implemented");
	}

	@Override
	public List<Event> collectCaseEvents(Case c) {

		List<Event> events = new ArrayList<Event>();

		String bestellung = (String) c.getIdentifier().get(EventAttributes.ID_BESTELLUNG);
		String bestellPos = (String) c.getIdentifier().get(EventAttributes.ID_BESTELL_POS);

		// 1) alle Events zu Bestellung + POS
		logger.debug("--< 1 >--");
		String sql = "select e.* from RAW_EVENTS_DATA best, RAW_EVENTS e, RAW_EVENTS_DATA pos "
				+ " where e.ID = best.EVENT_ID and best.KEY = '" + EventAttributes.ID_BESTELLUNG
				+ "' and best.VALUE = ? " + "   and e.ID = pos.EVENT_ID and pos.KEY = '"
				+ EventAttributes.ID_BESTELL_POS + "' and pos.VALUE = ? ";

//		template.queryForList("explain plan for " + sql).forEach(e -> logger.info("PLAN: {}", e));
				
		List<Event> tmp = template.query(sql, eventRowMapper, bestellung, bestellPos);
		tmp.forEach(e -> logger.debug("1) OEvents: {}", e));
		events.addAll(tmp);

		// 2) alle Events zu bestellung ohne POS
		logger.debug("--< 2 >--");
		sql = "select e.* from RAW_EVENTS_DATA best, RAW_EVENTS e "
				+ " where e.ID = best.EVENT_ID and best.KEY = '"
				+ EventAttributes.ID_BESTELLUNG
				+ "' and best.VALUE = ? and not exists (select null from RAW_EVENTS_DATA pos where e.ID = pos.EVENT_ID and KEY = '"
				+ EventAttributes.ID_BESTELL_POS + "')";
		
//		template.queryForList("explain plan for " + sql).forEach(e -> logger.info("PLAN: {}", e));

		tmp = template.query(sql, eventRowMapper, bestellung);
		tmp.forEach(e -> logger.debug("2) OEvents: {}", e));
		events.addAll(tmp);

		// 3) alle Kreditor Events zu KRED aus Bestellung
		logger.debug("--< 3 >--");
		String kreditor = events.stream().map(e -> (String) e.getAttributes().get(EventAttributes.ID_KREDITOR))
				.filter(k -> k != null).findAny().orElse(null);
		logger.debug("XEvents: {}", kreditor);

		if (kreditor != null) {
			sql = "select e.* from RAW_EVENTS e, RAW_EVENTS_DATA kred "
					+ " where e.ID = kred.EVENT_ID and EVENT_TYPE like'K%' " + " and kred.KEY = '"
					+ EventAttributes.ID_KREDITOR + "' and kred.VALUE = ? ";
			tmp = template.query(sql, eventRowMapper, kreditor);
			tmp.forEach(e -> logger.debug("3) Kreditor: {}", e));
			events.addAll(tmp);
		}

		// 4) Zahlung zu Rechnung
		logger.debug("--< 4 >--");
		// sql = "select distinct VALUE from RAW_EVENTS_DATA d where EVENT_ID in (400, 510, 511, 40, 41) and KEY = '"
		// + EventAttributes.ID_RECHNUNG + "'";
		// template.queryForList(sql).forEach(e -> logger.debug("Events: {}", e));

		String rechnung = events.stream().map(e -> (String) e.getAttributes().get(EventAttributes.ID_RECHNUNG))
				.filter(k -> k != null).findAny().orElse(null);
		logger.debug("XEvents: {}", rechnung);

		if (rechnung != null) {
			sql = "select e.* from RAW_EVENTS e, RAW_EVENTS_DATA inv "
					+ " where e.ID = inv.EVENT_ID and EVENT_TYPE = 'ZD' " + " and inv.KEY = '"
					+ EventAttributes.ID_RECHNUNG + "' and inv.VALUE = ? ";
			
//			template.queryForList("explain plan for " + sql).forEach(e -> logger.info("PLAN: {}", e));

			tmp = template.query(sql, eventRowMapper, rechnung);
			tmp.forEach(e -> logger.debug("4) Rechnung: {}", e));
			events.addAll(tmp);
		}

		events.sort((e1, e2) -> e1.getEventTs().compareTo(e2.getEventTs()));

		c.setEvents(events);

		// times
		c.setStartTs(events.stream().map(e -> e.getEventTs())
				.min(Comparator.comparing(e -> e, (t1, t2) -> t1.compareTo(t2))).orElse(null));
		c.setEndTs(events.stream().map(e -> e.getEventTs())
				.max(Comparator.comparing(e -> e, (t1, t2) -> t1.compareTo(t2))).orElse(null));
		c.setDuration(Duration.between(c.getStartTs(), c.getEndTs()));

		//footprint
		c.setFootprint(footprintGenerator.generate(c.getEvents()));
		
		return events;
	}

}
