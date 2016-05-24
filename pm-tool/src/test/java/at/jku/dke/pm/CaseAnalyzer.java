package at.jku.dke.pm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.jku.dke.pm.collect.BestellPosCaseCollector;
import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.repositories.JdbcCaseRepository;
import at.jku.dke.pm.web.model.graphlib.Edge;
import at.jku.dke.pm.web.model.graphlib.Graph;
import at.jku.dke.pm.web.model.graphlib.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CaseAnalyzer.class)
@Configuration
public class CaseAnalyzer {

	protected static final Logger logger = LoggerFactory.getLogger(CaseAnalyzer.class);

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
	public CaseRepository caseRepository() {
		return new JdbcCaseRepository(dataSource());
	}

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	protected CaseRepository caseRepository;

	// protected void addEvent(int processId, int caseId, int eventId, String activity, LocalDateTime time) {
	// logger.debug("EVENT: {} {}", activity, time);
	// jdbcTemplate.update("insert into EVENTS( CASE_ID, PROCESS_ID, ACTIVITY, ACTIVITY_TIME) values ?, ?, ?, ?",
	// caseId, processId, activity, Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
	//
	// }

	// @Test
	// public void generateFootprint() {
	// int processId = 2;
	// jdbcTemplate.queryForList(
	// "select count(*) || '|' || group_concat(activity order by activity_time ASC separator '|') from EVENTS "
	// + " where PROCESS_ID = ? and CASE_ID = ? group by PROCESS_ID, CASE_ID", processId, 1).forEach(
	// d -> logger.debug("FOOTPRINT: {}", d));
	//
	// logger.debug(
	// "update: {}",
	// jdbcTemplate
	// .update("update CASES c set FOOTPRINT = (select count(*) || '|' || group_concat(activity order by activity_time ASC separator '|') from EVENTS e "
	// + " where e.PROCESS_ID = c.PROCESS_ID and e.CASE_ID = c.ID group by PROCESS_ID, CASE_ID) where c.PROCESS_ID = ?",
	// processId));
	// // jdbcTemplate.update("truncate table CASES AND COMMIT");
	// }
	//

	@Test
	public void generateFootprint() {
		String processId = XESLoader.PROCESS_TELECLAIMS;
		processId = BestellPosCaseCollector.PROCESS_ID;

		// jdbcTemplate.queryForList(
		// "select count(*) || '|' || group_concat(EVENT_TYPE order by EVENT_TS ASC separator '|') from EVENTS "
		// + " where CASE_ID = ? group by CASE_ID", 10).forEach(
		// d -> logger.debug("FOOTPRINT: {}", d));

		logger.debug(
				"update: {}",
				jdbcTemplate
						.update("update CASES c set FOOTPRINT = (select lpad(count(*),4,'0') || '|' || group_concat(EVENT_TYPE order by EVENT_TS ASC separator '|') from EVENTS e "
								+ " where e.CASE_ID = c.ID group by CASE_ID) where c.PROCESS_ID = ?", processId));
		// jdbcTemplate.update("truncate table CASES AND COMMIT");
	}

	@Test
	public void sumFootprints() {
		String processId = XESLoader.PROCESS_TELECLAIMS;
		processId = BestellPosCaseCollector.PROCESS_ID;
		List<Map<String, Object>> data = jdbcTemplate
				.queryForList(
						"select count(*), FOOTPRINT from CASES where PROCESS_ID = ? group by FOOTPRINT order by 1 DESC, length(footprint) ASC",
						processId);
		dumpDataCvs(data);
		logger.debug("{} elements", data.size());
	}

	@Test
	public void dumpData() {
		String processId = XESLoader.PROCESS_TELECLAIMS;
		processId = BestellPosCaseCollector.PROCESS_ID;
		List<Map<String, Object>> data = jdbcTemplate.queryForList("select * from CASES where PROCESS_ID = ?",
				processId);
		dumpDataCvs(data);
	}

	@Test
	public void dumpRawEvents() {
		List<Map<String, Object>> data = jdbcTemplate.queryForList("select * from RAW_EVENTS");
		dumpDataCvs(data);
	}

	@Test
	public void dumpModels() {
		List<Map<String, Object>> data = jdbcTemplate.queryForList("select * from MODEL");
		dumpDataCvs(data);
	}

	public void dumpDataCvs(List<Map<String, Object>> data) {

		List<String> columns = new ArrayList<String>(data.get(0).keySet());

		StringBuffer res = new StringBuffer();
		res.append(columns.stream().collect(Collectors.joining(", "))).append('\n');

		for (Map<String, Object> line : data) {
			res.append(
					columns.stream().map(c -> line.get(c)).map(o -> o != null ? o : "").map(Object::toString)
							.map(s -> "'" + s + "'").collect(Collectors.joining(","))).append('\n');
		}
		logger.debug("DATA:\n{}", res.toString());

	}

	private static final String META_COUNT = "count";
	private static final String META_DURATION = "duration";

//	@SuppressWarnings("unchecked")
//	@Test
//	public void generateModel() {
//		String processId = XESLoader.PROCESS_TELECLAIMS;
//		processId = BestellPosCaseCollector.PROCESS_ID;
//
//		// alle löschen
//		logger.debug("delete model : {}", jdbcTemplate.update("delete from MODEL where PROCESS_ID = ?", processId));
//
//		// alle Footprints laden
//		List<String> footprints = jdbcTemplate.queryForList(
//				"select distinct FOOTPRINT from CASES where PROCESS_ID = ?", String.class, processId);
//
//		logger.debug("{} Footprints", footprints.size());
//		// nur test
//		// footprints.clear();
//		// footprints.add("0005|KA|BE|PE|BF|KG");
//
//		for (String fp : footprints) {
//			logger.debug("Analyze: {}", fp);
//
//			// Nodes/Edges für den Graph
//			Map<String, Node> nodes = new HashMap<>();
//			Map<String, Edge> edges = new HashMap<>();
//
//			List<Case> cases = caseRepository.findByFootprint(processId, fp);
//
//			Model m = new Model();
//			m.setProcessId(processId);
//			m.setFootprint(fp);
//			m.setNoCases(cases.size());
//
//			// Nodes vorbereiten
//			nodes.put("start", Node.create("start").label("Start").shape("circle").cssClass("pm_start"));
//			nodes.put("end", Node.create("end").label("End").shape("circle").cssClass("pm_end"));
//
//			// Cases analysieren
//			List<Duration> durations = new ArrayList<>();
//			for (Case c : cases) {
//				// 1) sort events
//				c.getEvents().sort((e1, e2) -> e1.getEventTs().compareTo(e2.getEventTs()));
//
//				// 2) Total Duration
//				Duration d = Duration.between(c.getEvents().get(0).getEventTs(),
//						c.getEvents().get(c.getEvents().size() - 1).getEventTs());
//				logger.debug("{} Duration {}", c.getId(), d);
//				durations.add(d);
//
//				// 3)graph aufbauen
//				Event last = null;
//				for (Event e : c.getEvents()) {
//					((AtomicInteger) nodes.computeIfAbsent(e.getType(), k -> Node.create(k).label(k)).getMeta()
//							.computeIfAbsent(META_COUNT, k -> new AtomicInteger(0))).incrementAndGet();
//
//					if (last != null) {
//						String l = last.getType();
//						Edge edge = edges.computeIfAbsent(String.format("%s-%s", last.getType(), e.getType()),
//								k -> Edge.create(l, e.getType()).label(k));
//						((AtomicInteger) edge.getMeta().computeIfAbsent(META_COUNT, k -> new AtomicInteger(0)))
//								.incrementAndGet();
//						((List<Duration>) edge.getMeta().computeIfAbsent(META_DURATION, k -> new ArrayList<Duration>()))
//								.add(Duration.between(last.getEventTs(), e.getEventTs()));
//
//						// edge.setValue(value);
//					} else {
//						Edge edge = edges.computeIfAbsent(String.format("start-%s", e.getType()),
//								k -> Edge.create("start", e.getType()).label(k));
//						((AtomicInteger) edge.getMeta().computeIfAbsent(META_COUNT, k -> new AtomicInteger(0)))
//								.incrementAndGet();
//
//					}
//
//					last = e;
//				}
//				String l = last.getType();
//				((AtomicInteger) edges
//						.computeIfAbsent(String.format("%s-end", last.getType()), k -> Edge.create(l, "end").label(k))
//						.getMeta().computeIfAbsent(META_COUNT, k -> new AtomicInteger(0))).incrementAndGet();
//
//			}
//
//			// durchschnittszeit
//			edges.values()
//					.stream()
//					.filter(e -> e.getMeta().containsKey(META_DURATION))
//					.forEach(
//							e -> {
//								logger.debug("e: X({}) {} {}", e, e.getValue(Edge.LABEL), e.getMeta());
//								e.getMeta().put(
//										META_DURATION,
//										Duration.ofSeconds((long) ((List<Duration>) e.getMeta(META_DURATION)).stream()
//												.mapToLong(Duration::getSeconds).average().orElse(0)));
//							});
//
//			// N/E logging
//			logger.debug("nodes: {}", nodes);
//			nodes.entrySet().forEach(e -> logger.debug("     N: {}: {}", e.getKey(), e.getValue().getMeta()));
//			logger.debug("edges: {}", edges);
//			edges.entrySet().forEach(e -> logger.debug("     E: {}: {}", e.getKey(), e.getValue().getMeta()));
//
//			// graph initialisieren
//			Graph g = new Graph(true, false, false);
//
//			nodes.values().forEach(n -> g.addNode(n));
//			edges.values().forEach(e -> g.addEdge(e));
//
//			m.setGraph(g);
//
//			// durations
//			m.setMinDuration(durations.stream().min(Comparator.comparing(Duration::getSeconds)).get());
//			m.setMaxDuration(durations.stream().max(Comparator.comparing(Duration::getSeconds)).get());
//			m.setAvgDuration(Duration.ofSeconds((long) durations.stream().mapToLong(Duration::getSeconds).average()
//					.orElse(0)));
//			logger.debug("Durations min:{} max:{} avg:{}", m.getMinDuration(), m.getMaxDuration(), m.getAvgDuration());
//
//			// logger.debug("avgPeriod   {}",DurationFormatUtils.formatDuration(m.getAvgDuration().getSeconds() * 1000,
//			// "d'D'H'H'm'M'"));
//
//			// store
//			jdbcTemplate
//					.update("insert into MODEL (PROCESS_ID, FOOTPRINT, NAME, NO_INSTANCES, MIN_DURATION, MAX_DURATION, AVG_DURATION, PROCESS_NET) values (?, ?, ?, ?, ?, ?, ?, ?)",
//							new PreparedStatementSetter() {
//
//								protected ObjectMapper mapper = new ObjectMapper().configure(
//										SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).registerModule(new JavaTimeModule());
//
//								@Override
//								public void setValues(PreparedStatement ps) throws SQLException {
//									ps.setString(1, m.getProcessId());
//									ps.setString(2, m.getFootprint());
//									ps.setString(3, null/* m.getName() */);
//									ps.setInt(4, m.getNoCases());
//									ps.setLong(5, m.getMinDuration().getSeconds());
//									ps.setLong(6, m.getMaxDuration().getSeconds());
//									ps.setLong(7, m.getAvgDuration().getSeconds());
//
//									String graph = null;
//									try {
//										graph = mapper.writeValueAsString(m.getGraph());
//									} catch (JsonProcessingException e) {
//										logger.error("JSON Error", e);
//									}
//									ps.setString(8, graph);
//								}
//							});
//		}
//	}

	protected final static Comparator<Duration> durationComparator = new Comparator<Duration>() {

		@Override
		public int compare(Duration arg0, Duration arg1) {
			return arg0.compareTo(arg1);
		}

	};

}
