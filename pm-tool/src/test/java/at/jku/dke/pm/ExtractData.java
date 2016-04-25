package at.jku.dke.pm;

import java.util.ArrayList;
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

import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.extract.BestellpositionExtractor;
import at.jku.dke.pm.extract.BestellungExtractor;
import at.jku.dke.pm.extract.EventExtractor;
import at.jku.dke.pm.extract.HistorieExtractor;
import at.jku.dke.pm.extract.KreditorExtractor;
import at.jku.dke.pm.extract.RechnungExtractor;
import at.jku.dke.pm.extract.WareneingangExtractor;
import at.jku.dke.pm.extract.ZahlungExtractor;
import at.jku.dke.pm.services.EventRepository;
import at.jku.dke.pm.services.repositories.JdbcEventRepository;

/**
 * Extrahieren von Events aus logfiles (DKE Beispiel Daten)
 * 
 * 1) DB Pfad anpassen @see at.jku.dke.pm.config.Filelocations
 * 2) DB mit Programm @see at.jku.dke.pm.config.CreateDB2
 * 3) external Foleder logs.example1.external in das DB Verzeichnis kopieren
 *  
 * 4) Text-Tabellen mit den csv Files verbinden: @see at.jku.dke.pm.ExtractData#bindFiles()
 * 5) Events extrahieren: @see at.jku.dke.pm.ExtractData#extractAll()
 * 
 * @author tremlro
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExtractData.class)
@Configuration
public class ExtractData {

	protected static final Logger logger = LoggerFactory.getLogger(ExtractData.class);

	@Bean
	public DataSource hsqlDataSource() {
		String dbUrl = String.format("jdbc:hsqldb:file:%s;hsqldb.script_format=3", Filelocations.HSQL_DB.getAbsolutePath());

		return DataSourceBuilder.create().driverClassName("org.hsqldb.jdbcDriver").url(dbUrl).username("SA")
				.password("").build();
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(hsqlDataSource());
	}

	@Bean
	public EventRepository eventRepository() {
		return new JdbcEventRepository(hsqlDataSource());
	}
	
	@Autowired
	protected JdbcTemplate template;
	
	@Autowired
	protected EventRepository eventRepository;

	@Test
	public void bindFiles() {
		template.execute("TRUNCATE TABLE BESTELLPOSITION AND COMMIT");
		template.update("SET TABLE DKE.BESTELLPOSITION SOURCE 'external/Bestellposition.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM BESTELLPOSITION").forEach(
				d -> logger.info("BESTELLPOSITION: {}", d));

		template.execute("TRUNCATE TABLE BESTELLUNG AND COMMIT");
		template.update("SET TABLE DKE.BESTELLUNG SOURCE 'external/Bestellung.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM BESTELLUNG").forEach(d -> logger.info("BESTELLUNG: {}", d));

		template.execute("TRUNCATE TABLE HISTORIE AND COMMIT");
		template.update("SET TABLE DKE.HISTORIE SOURCE 'external/Historie.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM HISTORIE").forEach(d -> logger.info("HISTORIE: {}", d));

		template.execute("TRUNCATE TABLE KREDITOR AND COMMIT");
		template.update("SET TABLE DKE.KREDITOR SOURCE 'external/Kreditor.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM KREDITOR").forEach(d -> logger.info("KREDITOR: {}", d));

		template.execute("TRUNCATE TABLE RECHNUNG AND COMMIT");
		template.update("SET TABLE DKE.RECHNUNG SOURCE 'external/Rechnung.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM RECHNUNG").forEach(d -> logger.info("RECHNUNG: {}", d));

		template.execute("TRUNCATE TABLE WARENEINGANG AND COMMIT");
		template.update("SET TABLE DKE.WARENEINGANG SOURCE 'external/Wareneingang.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM WARENEINGANG").forEach(d -> logger.info("WARENEINGANG: {}", d));

		template.execute("TRUNCATE TABLE ZAHLUNG AND COMMIT");
		template.update("SET TABLE DKE.ZAHLUNG SOURCE 'external/Zahlung.csv;ignore_first=true;encoding=UTF-8;fs=\\semi' DESC");
		template.queryForList("SELECT count(*) FROM ZAHLUNG").forEach(d -> logger.info("ZAHLUNG: {}", d));
	}

	@Test
	public void checkMD() {
		template.queryForList("SELECT count(*) FROM MD_EVENTS").forEach(d -> logger.info("MD_EVENTS: {}", d));
		template.queryForList("SELECT * FROM MD_EVENTS").forEach(d -> logger.info("MD_EVENTS: {}", d));
	}

	@Test
	public void truncateRawEvents() {
		template.execute("TRUNCATE TABLE RAW_EVENTS_DATA AND COMMIT");
		template.execute("TRUNCATE TABLE RAW_EVENTS AND COMMIT");
	}

	@Test
	public void extractTest() {
		List<Map<String, Object>> data;
		
		data = template.queryForList("select * from HISTORIE limit 1");
		data.forEach(d -> logger.info("DATA: {}", d));
		
		EventExtractor ex = new HistorieExtractor();
		
		List<Event> events = ex.extractEvents(data.get(0));
		events.forEach(e -> logger.debug("Events: {}", e));
	}
	
	@Test
	public void extractAndStoreTest() {
		List<Map<String, Object>> data;
		
		data = template.queryForList("select * from KREDITOR limit 1");
		data.forEach(d -> logger.info("KREDITOR: {}", d));
		
		EventExtractor ex = new KreditorExtractor();
		
		List<Event> events = ex.extractEvents(data.get(0));
		events.forEach(e -> logger.debug("Events: {}", e));
		
		eventRepository.saveAll(events);
		
		template.queryForList("SELECT count(*) FROM RAW_EVENTS").forEach(d -> logger.info("RAW_EVENTS: {}", d));
		template.queryForList("SELECT * FROM RAW_EVENTS").forEach(d -> logger.info("RAW_EVENTS: {}", d));		
	}

	
	@Test
	public void extract() {
		List<Map<String, Object>> data;
		
		data = template.queryForList("select * from KREDITOR");
		
		EventExtractor ex = new KreditorExtractor();

		List<Event> events = new ArrayList<>();
		
		data.forEach(d -> {
			events.addAll(ex.extractEvents(d));
		});
		
		events.forEach(e -> logger.debug("Events: {}", e));
		
		eventRepository.saveAll(events);
	}

	@Test
	public void extractAll() {
		List<Event> events = new ArrayList<>();
		
		EventExtractor exk = new KreditorExtractor();
		template.queryForList("select * from KREDITOR").forEach(d -> {
			events.addAll(exk.extractEvents(d));
		});

		EventExtractor exb = new BestellungExtractor();
		template.queryForList("select * from BESTELLUNG").forEach(d -> {
			events.addAll(exb.extractEvents(d));
		});

		EventExtractor exp = new BestellpositionExtractor();
		template.queryForList("select * from BESTELLPOSITION").forEach(d -> {
			events.addAll(exp.extractEvents(d));
		});

		EventExtractor exw = new WareneingangExtractor();
		template.queryForList("select * from WARENEINGANG").forEach(d -> {
			events.addAll(exw.extractEvents(d));
		});

		EventExtractor exr = new RechnungExtractor();
		template.queryForList("select * from RECHNUNG").forEach(d -> {
			events.addAll(exr.extractEvents(d));
		});
		
		EventExtractor exz = new ZahlungExtractor();
		template.queryForList("select * from ZAHLUNG").forEach(d -> {
			events.addAll(exz.extractEvents(d));
		});

		EventExtractor exh = new HistorieExtractor();
		template.queryForList("select * from HISTORIE").forEach(d -> {
			events.addAll(exh.extractEvents(d));
		});

		events.forEach(e -> logger.debug("Events: {}", e));
		
		eventRepository.saveAll(events);
		logger.debug("{} Events", events.size());
	}

	@Test
	public void info() {
		template.queryForList("select count(*) from RAW_EVENTS").forEach(e -> logger.debug("Events: {}", e));
		template.queryForList("select count(*) from BESTELLPOSITION").forEach(e -> logger.debug("pos: {}", e));

	}

}
