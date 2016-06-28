package at.jku.dke.pm;

import static org.joox.JOOX.$;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.el.stream.Stream;
import org.joox.JOOX;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import at.jku.dke.pm.analyze.BasicModelAnalyzer;
import at.jku.dke.pm.analyze.ModelAnalyzer;
import at.jku.dke.pm.collect.BestellPosCaseCollector;
import at.jku.dke.pm.collect.CaseCollector;
import at.jku.dke.pm.collect.footprint.FootprintGenerator;
import at.jku.dke.pm.collect.footprint.SimpleFootprintGenerator;
import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.Events;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.repositories.JdbcCaseRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XESLoader.class)
@Configuration
public class XESLoader {

	protected static final Logger logger = LoggerFactory.getLogger(XESLoader.class);

	// private final static File ROOT = new File("C:\\appl\\ws\\LB-SSC\\pm-tool\\src\\test\\resources\\xes");
	private final static File ROOT = new File("C:\\appl\\github\\dke-ss16-pm\\pm-tool\\src\\test\\resources\\xes");

	public static final String PROCESS_REVIEW = "review";
	public static final String PROCESS_TELECLAIMS = "teleclaims";
	public static final String PROCESS_REPAIR = "repair";
	public static final String PROCESS_REPAIR2 = "repair2";

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

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Test
	public void simpleTest() {
		File in = new File(ROOT, "reviewing.xes");
		logger.debug("in: {}", in.exists());
	}

	protected static final String NAME = "concept:name";
	protected static final String TIME = "time:timestamp";

	@Test
	public void loadReviewing() throws Exception {
		loadXES(PROCESS_REVIEW, new File(ROOT, "reviewing.xes"));
	}

	@Test
	public void loadRepairExample() throws Exception {
		String processId = PROCESS_REPAIR;
//		jdbcTemplate.update("delete from PROCESS where ID = ?", processId);
//		loadXES(processId, new File(ROOT, "repairExample.xes"));
//
//		eventTypes(processId);
//		generateFootprints(processId);
//		step4_AnalyzeCases(processId);
	}

	@Test
	public void loadRepairExample2() throws Exception {
		String processId = PROCESS_REPAIR2;
		jdbcTemplate.update("delete from CASES where PROCESS_ID = ?", processId);
		loadXES(processId, new File(ROOT, "repairExampleSample2.xes"));

		eventTypes(processId);
		generateFootprints(processId);
		step4_AnalyzeCases(processId);
	}

	@Test
	public void loadTeleclaims() throws Exception {
		String processId = PROCESS_TELECLAIMS;
		jdbcTemplate.update("delete from CASES where PROCESS_ID = ?", processId);
		loadXES(processId, new File(ROOT, "teleclaims.xes"));
		
		eventTypes(processId);
		generateFootprints(processId);
		step4_AnalyzeCases(processId);
	}

	public void loadXES(String processId, File xesFile) throws Exception {
		Document doc = JOOX.builder().parse(xesFile);

		$(doc).find("trace").forEach(
				ctx -> {
					int caseId = addCase(processId, $(ctx).children().matchAttr("key", NAME).attr("value"));
					AtomicInteger idx = new AtomicInteger(0);
					$(ctx).children("event").forEach(
							eCtx -> {
								addEvent(caseId, idx.getAndIncrement(),
										$(eCtx).children().matchAttr("key", NAME).attr("value"), LocalDateTime.parse(
												$(eCtx).children().matchAttr("key", TIME).attr("value"),
												DateTimeFormatter.ISO_OFFSET_DATE_TIME));
							});
				});
	}

	protected int addCase(String processId, String name) {
		logger.debug("CASE: {} {}", processId, name);

		GeneratedKeyHolder holder = new GeneratedKeyHolder();
		jdbcTemplate.update(new CasePSC(processId, name), holder);

		return holder.getKey().intValue();
	}

	protected void addEvent(int caseId, int id, String eventId, LocalDateTime time) {
		logger.debug("EVENT: {} {}", eventId, time);
		jdbcTemplate.update("insert into EVENTS( CASE_ID, ID, EVENT_TYPE, EVENT_TS) values ?, ?, ?, ?", caseId, id,
				eventId, Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));

	}

	public static class CasePSC implements PreparedStatementCreator {

		protected final String SQL = "insert into CASES( PROCESS_ID, NAME ) values (?, ?)";
		protected final String processId;

		protected final String name;

		public CasePSC(String processId, String name) {
			this.processId = processId;
			this.name = name;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			PreparedStatement ps = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, processId);
			ps.setString(2, name);

			return ps;
		}

	}

	@Test
	public void truncateData() {
		jdbcTemplate.update("truncate table CASES AND COMMIT");
		jdbcTemplate.update("truncate table EVENTS AND COMMIT");
	}

	@Test
	public void checkData() {
		jdbcTemplate.queryForList("select count(*) from CASES").forEach(d -> logger.debug("CASES: {}", d));
		jdbcTemplate.queryForList("select count(*) from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
	}

	@Test
	public void dumpData() {
		jdbcTemplate.queryForList("select * from CASES").forEach(d -> logger.debug("CASES: {}", d));
		jdbcTemplate.queryForList("select * from EVENTS order by case_id, id").forEach(
				d -> logger.debug("EVENTS: {}", d));
	}

	@Test
	public void dumpData2() {
		jdbcTemplate.queryForList("select * from CASES").forEach(d -> logger.debug("CASES: {}", d));
		jdbcTemplate.queryForList("select * from EVENTS, CASES where cases.id = case_id and  PROCESS_ID = ? ",
				PROCESS_TELECLAIMS).forEach(d -> logger.debug("EVENTS: {}", d));
	}

	@Test
	public void dumpProcess() {
		jdbcTemplate.queryForList("select * from PROCESS").forEach(d -> logger.debug("PROCESS: {}", d));

	}

//	@Test
	public void eventTypes(String processId) {
//		String processId = PROCESS_TELECLAIMS;
		jdbcTemplate.queryForList(
				"select distinct(EVENT_TYPE) from EVENTS, CASES where cases.id = case_id and  PROCESS_ID = ? ",
				processId).forEach(d -> logger.debug("EVENTS: {}", d));
		jdbcTemplate.update("delete from DKE.MD_EVENTS where PROCESS_ID = ?", processId);
		jdbcTemplate.queryForList("select PROCESS_ID, count(*) from MD_EVENTS group by process_id order by process_id")
				.forEach(d -> logger.debug("EVENTS: {}", d));

		jdbcTemplate.update("delete from PROCESS where ID = ?", processId);
		jdbcTemplate.update("insert into PROCESS values (?, ?)", processId, processId);

		AtomicInteger cnt = new AtomicInteger(0);
		jdbcTemplate.queryForList(
				"select distinct(EVENT_TYPE) from EVENTS, CASES where cases.id = case_id and  PROCESS_ID = ? ",
				processId).forEach(
				e -> jdbcTemplate.update("insert into DKE.MD_EVENTS values (?, ?, ?)", processId,
						buildId(cnt.getAndIncrement(), (String) e.get("EVENT_TYPE")), e.get("EVENT_TYPE")));

		updateEvents(processId);
	}

	@Test
	public void dumpEvents() {
		jdbcTemplate.queryForList("select * from MD_EVENTS order by process_id")
				.forEach(d -> logger.debug("EVENTS: {}", d));
		jdbcTemplate.queryForList("select PROCESS_ID, count(*) from MD_EVENTS group by process_id order by process_id")
		.forEach(d -> logger.debug("EVENTS: {}", d));	}

//	@Test
	public void updateEvents(String processId) {
//		String processId = PROCESS_TELECLAIMS;

		logger.debug(
				"update: {}",
				jdbcTemplate
						.update("update EVENTS  set EVENT_TYPE = (select ID from MD_EVENTS where name = EVENT_TYPE) where exists (select null from CASES where cases.id = case_id and  PROCESS_ID = ?)",
								processId));
	}

	@Test
	public void deleteMDEvents() {
		jdbcTemplate.update("delete from DKE.MD_EVENTS where PROCESS_ID = ?", PROCESS_REPAIR);
	}

	private String buildId(int cnt, String name) {
		String id = Arrays.stream(name.toUpperCase().replaceAll("[^\\w\\s]", "").split("(?<=[\\S])[\\S]*\\s*")).collect(Collectors.joining());
		return cnt + id;
	}

	@Test
	public void splittest() {
		String name = "determine likelihood of claim (foo)";
		logger.debug("split {}",name.toUpperCase().replaceAll("[^\\w\\s]", ""));
		logger.debug("split {} {}", buildId(0, name), name.split("(?<=[\\S])[\\S]*\\s*"));
	}

	protected FootprintGenerator footprintGenerator = new SimpleFootprintGenerator();

//	@Test
	public void generateFootprints(String processId) {

		List<Case> cases = new ArrayList<>();
		
		jdbcTemplate.queryForList("select ID from CASES where PROCESS_ID = ?", processId).forEach(
				d -> {					
					logger.debug("CASE: {}", d);
					Case c = caseRepository.findById((int) d.get("ID"));
					List<Event> events = c.getEvents();
					events.sort((e1, e2) -> e1.getEventTs().compareTo(e2.getEventTs()));
					
					// times
					c.setStartTs(events.stream().map(e -> e.getEventTs())
							.min(Comparator.comparing(e -> e, (t1, t2) -> t1.compareTo(t2))).orElse(null));
					c.setEndTs(events.stream().map(e -> e.getEventTs())
							.max(Comparator.comparing(e -> e, (t1, t2) -> t1.compareTo(t2))).orElse(null));
					c.setDuration(Duration.between(c.getStartTs(), c.getEndTs()));

					//footprint
					c.setFootprint(footprintGenerator.generate(events));
					cases.add(c);
				});
		
		cases.forEach(c -> {
			caseRepository.save(c);
		});
	}
	
//	@Test
//	public void debugCase() {
//		Case c = caseRepository.findById(3707);
//		logger.debug("case: {}", c);
//	}

	
	@Test
	public void dumpFootprint() {
		jdbcTemplate.queryForList("select FOOTPRINT, count(*) from CASES where PROCESS_ID = ? group by footprint order by 2 desc", PROCESS_TELECLAIMS)
				.forEach(d -> logger.debug("EVENTS: {}", d));
		
		jdbcTemplate.queryForList("select * from MODEL where FOOTPRINT = ?","17|9IC|0BCISIIA|0BCISIIA|1BRC|1BRC|7DLOC|5AC|7DLOC|4ACOR|5AC|10IP|4ACOR|10IP|6CC|6CC|8E|8E")
		.forEach(d -> logger.debug("EVENTS: {}", d));		
	}

	// Step 4

	@Bean
	public CaseRepository caseRepository() {
		return new JdbcCaseRepository(dataSource());
	}

	@Bean
	public ModelAnalyzer modelAnalyzer() {
		return new BasicModelAnalyzer();
	}

	@Autowired
	protected CaseRepository caseRepository;

	@Autowired
	protected ModelAnalyzer modelAnalyzer;

//	@Test
	public void step4_AnalyzeCases(String processId) {
		logger.debug("step 4: analyze cases");

//		String processId = PROCESS_TELECLAIMS;

		// alle löschen
		logger.debug("delete model : {}", jdbcTemplate.update("delete from MODEL where PROCESS_ID = ?", processId));

		// alle Footprints laden
		List<String> footprints = jdbcTemplate.queryForList(
				"select distinct FOOTPRINT from CASES where PROCESS_ID = ?", String.class, processId);

		logger.debug("{} Footprints", footprints.size());

		for (String fp : footprints) {
			logger.debug("Analyze: {}", fp);

			List<Case> cases = caseRepository.findByFootprint(processId, fp);

			Model m = new Model();
			m.setProcessId(processId);
			m.setFootprint(fp);

			modelAnalyzer.analyze(m, cases);

			jdbcTemplate
					.update("insert into MODEL (PROCESS_ID, FOOTPRINT, NAME, NO_INSTANCES, MIN_DURATION, MAX_DURATION, AVG_DURATION, PROCESS_NET) values (?, ?, ?, ?, ?, ?, ?, ?)",
							new PreparedStatementSetter() {

								protected ObjectMapper mapper = new ObjectMapper().configure(
										SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).registerModule(
										new JavaTimeModule());

								@Override
								public void setValues(PreparedStatement ps) throws SQLException {
									ps.setString(1, m.getProcessId());
									ps.setString(2, m.getFootprint());
									ps.setString(3, null/* m.getName() */);
									ps.setInt(4, m.getNoCases());
									ps.setLong(5, m.getMinDuration().getSeconds());
									ps.setLong(6, m.getMaxDuration().getSeconds());
									ps.setLong(7, m.getAvgDuration().getSeconds());

									String net = null;
									try {
										net = mapper.writeValueAsString(m.getProcessNet());
									} catch (JsonProcessingException e) {
										logger.error("JSON Error", e);
									}
									ps.setString(8, net);
								}
							});

		}

	}
}
