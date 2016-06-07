package at.jku.dke.pm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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

import at.jku.dke.pm.analyze.BasicModelAnalyzer;
import at.jku.dke.pm.analyze.ModelAnalyzer;
import at.jku.dke.pm.collect.BestellPosCaseCollector;
import at.jku.dke.pm.collect.CaseCollector;
import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.Events;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.extract.BestellpositionExtractor;
import at.jku.dke.pm.extract.BestellungExtractor;
import at.jku.dke.pm.extract.EventExtractor;
import at.jku.dke.pm.extract.HistorieExtractor;
import at.jku.dke.pm.extract.KreditorExtractor;
import at.jku.dke.pm.extract.RechnungExtractor;
import at.jku.dke.pm.extract.WareneingangExtractor;
import at.jku.dke.pm.extract.ZahlungExtractor;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.EventRepository;
import at.jku.dke.pm.services.repositories.JdbcCaseRepository;
import at.jku.dke.pm.services.repositories.JdbcEventRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@Configuration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Application {

	protected static final Logger logger = LoggerFactory.getLogger(Application.class);

	/*
	 * Configuration
	 */
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
	public EventRepository eventRepository() {
		return new JdbcEventRepository(dataSource());
	}

	@Bean
	public CaseRepository caseRepository() {
		return new JdbcCaseRepository(dataSource());
	}

	@Bean
	public CaseCollector caseCollector() {
		return new BestellPosCaseCollector(dataSource());
	}

	@Bean
	public ModelAnalyzer modelAnalyzer() {
		return new BasicModelAnalyzer();
	}

	/*
	 * Application
	 */
	@Autowired
	protected JdbcTemplate template;

	@Autowired
	protected EventRepository eventRepository;

	@Autowired
	protected CaseRepository caseRepository;

	@Autowired
	protected CaseCollector collector;

	@Autowired
	protected ModelAnalyzer modelAnalyzer;

	/*
	 * Data
	 * <Table> -> <.csv file>, <extractor>
	 */
	@SuppressWarnings("serial")
	protected static final Map<String, InputDataTable> DATA = new HashMap<String, InputDataTable>() {
		{
			put("KREDITOR", new InputDataTable("Kreditor.csv", new KreditorExtractor()));
			put("BESTELLUNG", new InputDataTable("Bestellung.csv", new BestellungExtractor()));
			put("BESTELLPOSITION", new InputDataTable("Bestellposition.csv", new BestellpositionExtractor()));
			put("WARENEINGANG", new InputDataTable("Wareneingang.csv", new WareneingangExtractor()));
			put("RECHNUNG", new InputDataTable("Rechnung.csv", new RechnungExtractor()));
			put("ZAHLUNG", new InputDataTable("Zahlung.csv", new ZahlungExtractor()));
			put("HISTORIE", new InputDataTable("Historie.csv", new HistorieExtractor()));
		}
	};

	/*
	 * Extract
	 */
	@Test
	public void step1_BindFiles() {
		logger.debug("step 1: bind files");

		DATA.entrySet().forEach(
				e -> {
					template.execute(String.format("TRUNCATE TABLE %s AND COMMIT", e.getKey()));
					template.update(String.format(
							"SET TABLE DKE.%s SOURCE 'external/%s;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC",
							e.getKey(), e.getValue().getFile()));
					template.queryForList(String.format("SELECT count(*) FROM %s", e.getKey())).forEach(
							d -> logger.info("{}: {}", e.getKey(), d));
				});
	}

	@Test
	public void step2_ExtractEvents() {
		logger.debug("step 2: extract events");

		template.execute("delete FROM RAW_EVENTS");

		List<Event> events = new ArrayList<>();

		DATA.entrySet().forEach(
				e -> {
					template.queryForList(String.format("select * from %s", e.getKey())).forEach(
							d -> events.addAll(e.getValue().getExtractor().extractEvents(d)));
				});

		events.forEach(e -> logger.debug("Events: {}", e));

		eventRepository.saveAll(events);
		logger.debug("{} Events", events.size());
	}

	/*
	 * Collect Cases
	 */
	@Test
	public void step3_CollectCases() {
		logger.debug("step 3: collec cases");

		logger.debug("delete process : {}",
				template.update("delete from PROCESS where ID = ?", BestellPosCaseCollector.PROCESS_ID));

		logger.debug("delete cases   : {}",
				template.update("delete from CASES where PROCESS_ID = ?", BestellPosCaseCollector.PROCESS_ID));

		logger.debug("insert process : {}", template.update("insert into PROCESS (ID, NAME) values (?, ?)",
				BestellPosCaseCollector.PROCESS_ID, "DKE Praktikum"));

		Events.MD_EVENTS.entrySet().forEach(
				e -> template.update("insert into DKE.MD_EVENTS values (?, ?, ?)", BestellPosCaseCollector.PROCESS_ID,
						e.getKey(), e.getValue()));

		// cases laden
		List<Case> cases = collector.identifyCases();
		logger.debug("{} cases", cases.size());

		cases.forEach(c -> {
			collector.collectCaseEvents(c);
			caseRepository.save(c);
		});

	}

	/*
	 * Collect Cases
	 */
	@Test
	public void step4_AnalyzeCases() {
		logger.debug("step 4: analyze cases");

		String processId = BestellPosCaseCollector.PROCESS_ID;

		// alle löschen
		logger.debug("delete model : {}", template.update("delete from MODEL where PROCESS_ID = ?", processId));

		// alle Footprints laden
		List<String> footprints = template.queryForList("select distinct FOOTPRINT from CASES where PROCESS_ID = ?",
				String.class, processId);

		logger.debug("{} Footprints", footprints.size());

		for (String fp : footprints) {
			logger.debug("Analyze: {}", fp);

			List<Case> cases = caseRepository.findByFootprint(processId, fp);

			Model m = new Model();
			m.setProcessId(processId);
			m.setFootprint(fp);

			modelAnalyzer.analyze(m, cases);

			template.update(
					"insert into MODEL (PROCESS_ID, FOOTPRINT, NAME, NO_INSTANCES, MIN_DURATION, MAX_DURATION, AVG_DURATION, PROCESS_NET) values (?, ?, ?, ?, ?, ?, ?, ?)",
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

	/*
	 * Data Configuration
	 */
	public static class InputDataTable {
		protected final String file;
		protected final EventExtractor extractor;

		public InputDataTable(String file, EventExtractor extractor) {
			this.file = file;
			this.extractor = extractor;
		}

		public String getFile() {
			return file;
		}

		public EventExtractor getExtractor() {
			return extractor;
		}

	}
}
