package at.jku.dke.pm;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.jku.dke.pm.collect.BestellPosCaseCollector;
import at.jku.dke.pm.collect.CaseCollector;
import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.Events;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.repositories.JdbcCaseRepository;
import at.jku.dke.pm.web.model.graphlib.Edge;
import at.jku.dke.pm.web.model.graphlib.Graph;
import at.jku.dke.pm.web.model.graphlib.Node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DkeCaseCollector.class)
@Configuration
public class DkeCaseCollector {

	protected static final Logger logger = LoggerFactory.getLogger(DkeCaseCollector.class);

	@Bean
	public DataSource dataSource() {
		String dbUrl = String.format("jdbc:hsqldb:file:%s;hsqldb.script_format=3",
				Filelocations.HSQL_DB.getAbsolutePath());

		return DataSourceBuilder.create().driverClassName("org.hsqldb.jdbcDriver").url(dbUrl).username("SA")
				.password("").build();
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean
	public CaseCollector caseCollector() {
		return new BestellPosCaseCollector(dataSource());
	}

	@Bean
	public CaseRepository caseRepository() {
		return new JdbcCaseRepository(dataSource());
	}

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	protected CaseCollector collector;

	@Autowired
	protected CaseRepository caseRepository;

	@Test
	public void identifyTest() {
		List<Case> cases = collector.identifyCases();
		cases.forEach(c -> logger.debug("CASE: {}", c));
	}

	@Test
	public void singleCollectTest() {
		// cases laden
		Case c = caseRepository.findById(191);
		logger.debug("CASE: {}", c);

		List<Event> events = collector.collectCaseEvents(c);

		events.forEach(e -> logger.debug("E> {}", e));

		caseRepository.save(c);
		logger.debug("CASE: {}", c);

	}

	@Test
	public void extractDkeCases() {

		logger.debug("delete process : {}",
				jdbcTemplate.update("delete from PROCESS where ID = ?", BestellPosCaseCollector.PROCESS_ID));

		logger.debug("delete cases   : {}",
				jdbcTemplate.update("delete from CASES where PROCESS_ID = ?", BestellPosCaseCollector.PROCESS_ID));

		logger.debug("insert process : {}", jdbcTemplate.update("insert into PROCESS (ID, NAME) values (?, ?)",
				BestellPosCaseCollector.PROCESS_ID, "DKE Praktikum"));

		Events.MD_EVENTS.entrySet().forEach(
				e -> jdbcTemplate.update("insert into DKE.MD_EVENTS values (?, ?, ?)",
						BestellPosCaseCollector.PROCESS_ID, e.getKey(), e.getValue()));

		// cases laden
		List<Case> cases = collector.identifyCases();
		logger.debug("{} cases", cases.size());

		cases.forEach(c -> {
			collector.collectCaseEvents(c);
			caseRepository.save(c);
		});

	}

	@Test
	public void deleteDkeCases() {
		jdbcTemplate.queryForList("select PROCESS_ID, count(*) from CASES group by PROCESS_ID").forEach(
				d -> logger.debug("PID: {}", d));

		logger.debug("delete cases : {}",
				jdbcTemplate.update("delete from CASES where PROCESS_ID = ?", BestellPosCaseCollector.PROCESS_ID));

		// logger.debug("delete cases : {}",
		// jdbcTemplate.update("insert into PROCESS (ID, NAME) values (?, ?)", BestellPosCaseCollector.PROCESS_ID,
		// "DKE Praktikum"));

	}

	@Test
	public void saveTest() {
		// cases laden
		Case c = caseRepository.findById(188);
		logger.debug("CASE: {}", c);

		c.setName("MODIFIED");
		logger.debug("M1 CASE: {}", c);
		caseRepository.save(c);

		logger.debug("M2 CASE: {}", c);
	}

	@Test
	public void countTest() {
		jdbcTemplate.queryForList("select count(*) from CASES").forEach(d -> logger.debug("CASES: {}", d));
		jdbcTemplate.queryForList("select count(*) from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
		jdbcTemplate.queryForList("select count(*) from EVENTS_DATA").forEach(d -> logger.debug("EVENTS_DATA: {}", d));

	}

	@Test
	public void xxxTest() {
		jdbcTemplate.queryForList("select count(*) from CASES").forEach(d -> logger.debug("CASES: {}", d));
		jdbcTemplate.queryForList("select count(*) from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
		jdbcTemplate.queryForList("select count(*) from EVENTS_DATA").forEach(d -> logger.debug("EVENTS_DATA: {}", d));
		jdbcTemplate.queryForList("select * from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
		jdbcTemplate.queryForList("select * from EVENTS_DATA").forEach(d -> logger.debug("EVENTS_DATA: {}", d));
	}

	@Test
	public void xxxyyyTest() {
		jdbcTemplate.queryForList("select * from RAW_EVENTS where EVENT_TYPE like 'K%'").forEach(
				d -> logger.debug("EVENTS: {}", d));

		jdbcTemplate.queryForList("select * from RAW_EVENTS_DATA where EVENT_ID in (631)").forEach(
				d -> logger.debug("EVENTS_DATA: {}", d));

		jdbcTemplate.queryForList("select * from EVENTS where ID in (11,631,632,630)").forEach(
				d -> logger.debug("->EVENTS: {}", d));

		jdbcTemplate.queryForList("select * from RAW_EVENTS_DATA where KEY = 'ID_KREDITOR' and VALUE = '1000012'")
				.forEach(d -> logger.debug("1000012: {}", d));

		jdbcTemplate
				.queryForList(
						"select * from RAW_EVENTS, RAW_EVENTS_DATA where raw_events.id = event_id and KEY = 'ID_KREDITOR' order by VALUE, EVENT_TS")
				.forEach(d -> logger.debug("??????: {}", d));

		// jdbcTemplate.queryForList("select count(*) from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
		// jdbcTemplate.queryForList("select * from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
		// jdbcTemplate.queryForList("select * from EVENTS_DATA").forEach(d -> logger.debug("EVENTS_DATA: {}", d));
	}

	@Test
	public void missingTest() {
		// jdbcTemplate.queryForList("select * from raw_EVENTS r where r.id is null").forEach(d ->
		// logger.debug("MISS: {}", d));
		jdbcTemplate.queryForList(
				"select r.* from RAW_EVENTS r LEFT OUTER JOIN EVENTS e on (r.id = e.id) where e.id is null").forEach(
				d -> logger.debug("MISS: {}", d));

		jdbcTemplate.queryForList("select * from EVENTS where ID in (1742,1743)").forEach(
				d -> logger.debug("->EVENTS: {}", d));
		jdbcTemplate
				.queryForList(
						"select * from RAW_EVENTS, RAW_EVENTS_DATA where raw_events.id = event_id and raw_events.id in (1742,1743) order by event_id, VALUE, EVENT_TS")
				.forEach(d -> logger.debug("??????: {}", d));

	}

	@Test
	public void yyyTest() {

		jdbcTemplate
				.queryForList(
						"select * from RAW_EVENTS, RAW_EVENTS_DATA where raw_events.id = event_id and KEY = 'ID_RECHNUNG' and VALUE = '20000061'")
				.forEach(d -> logger.debug("20000061: {}", d));

		jdbcTemplate
				.queryForList(
						"select * from RAW_EVENTS, RAW_EVENTS_DATA where raw_events.id = event_id and KEY = 'ID_RECHNUNG' order by VALUE, EVENT_TS")
				.forEach(d -> logger.debug("??????: {}", d));

	}

	@Test
	public void mdEventsTest() {

		jdbcTemplate.queryForList("select * from MD_EVENTS").forEach(d -> logger.debug("MdEvents: {}", d));

	}

	@Test
	public void d3ProcessGraphTest() throws Exception {

		Case c = caseRepository.findById(180);
		logger.debug("C = {}", c);

		// jdbcTemplate.queryForList("select * from CASES limit 5").forEach(d -> logger.debug("case: {}", d));

		Graph g = new Graph(true, false, false);

		g.addNode(Node.create("start").label("Start").cssClass("pm_start"));
		Event last = null;
		for (Event e : c.getEvents()) {
			g.addNode(Node.create(String.valueOf(e.getId())).label(e.getType() + " " + e.getEventTs()));

			if (last != null) {
				g.addEdge(Edge.create(String.valueOf(last.getId()), String.valueOf(e.getId())).label(
						Duration.between(last.getEventTs(), e.getEventTs()).toString()));
			} else {
				g.addEdge(Edge.create("start", String.valueOf(e.getId())));
			}
			last = e;
		}
		g.addNode(Node.create("end").label("End").cssClass("pm_end"));
		g.addEdge(Edge.create(String.valueOf(last.getId()), "end"));

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		// mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		// SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

		// mapper.setDateFormat(outputFormat);

		String res = mapper.writeValueAsString(g);

		logger.debug("GRAPH\n{}", res);

	}

	@Test
	public void d3GraphTest() throws Exception {

		Map<String, Node> nodes = new HashMap<>();
		Map<String, Edge> edges = new HashMap<>();

		String processId = XESLoader.PROCESS_TELECLAIMS;
		// processId = BestellPosCaseCollector.PROCESS_ID;
		List<Integer> ids = jdbcTemplate.queryForList("select ID from cases where process_id = ?", Integer.class,
				processId);

		for (Integer id : ids) {
			Case c = caseRepository.findById(id);
			logger.debug("C = {}", c);

			// jdbcTemplate.queryForList("select * from CASES limit 5").forEach(d -> logger.debug("case: {}", d));

			nodes.put("start", Node.create("start").label("Start").cssClass("pm_start"));
			nodes.put("end", Node.create("end").label("End").cssClass("pm_end"));

			Event last = null;
			for (Event e : c.getEvents()) {
				Node n = nodes.computeIfAbsent(e.getType(), k -> Node.create(k).label(k));

				if (last != null) {
					String l = last.getType();
					Edge edge = edges.computeIfAbsent(String.format("%s-%s", last.getType(), e.getType()), k -> Edge
							.create(l, e.getType()).label(k));
					// edge.setValue(value);
				} else {
					Edge edge = edges.computeIfAbsent(String.format("start-%s", e.getType()),
							k -> Edge.create("start", e.getType()).label(k));
				}

				last = e;
			}
			String l = last.getType();
			Edge edge = edges.computeIfAbsent(String.format("%s-end", last.getType()), k -> Edge.create(l, "end")
					.label(k));

		}
		// graph initialisieren
		Graph g = new Graph(true, false, false);

		nodes.values().forEach(n -> g.addNode(n));
		edges.values().forEach(e -> g.addEdge(e));

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		// mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		// SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

		// mapper.setDateFormat(outputFormat);

		String res = mapper.writeValueAsString(g);

		logger.debug("GRAPH\n{}", res);

	}

}
