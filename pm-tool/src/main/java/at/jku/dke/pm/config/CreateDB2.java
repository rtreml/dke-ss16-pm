package at.jku.dke.pm.config;

import java.io.File;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

public class CreateDB2 {

	protected static final Logger logger = LoggerFactory.getLogger(CreateDB2.class);

	protected final DataSource dataSource;

	protected final JdbcTemplate jdbcTemplate;

	public CreateDB2(File dbUrlFile) {
		String dbUrl = String.format("jdbc:hsqldb:file:%s;hsqldb.script_format=3", dbUrlFile.getAbsolutePath());
		logger.debug("URL: {}", dbUrl);
		dataSource = DataSourceBuilder.create().driverClassName("org.hsqldb.jdbcDriver").url(dbUrl).username("SA")
				.password("").build();

		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	protected void db_00_createSchema() {
		logger.debug("db_00_createSchema");

		logger.debug("db_00_createSchema:USER");
		jdbcTemplate.update("DROP SCHEMA IF EXISTS DKE CASCADE");
		// falls DKE das initial Schema war dann wird es nicht komplett gelöscht.
		int schemaExists = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'DKE'", Integer.class);
		if (schemaExists == 0) {
			jdbcTemplate.update("CREATE SCHEMA DKE");
			jdbcTemplate.update("SET DATABASE DEFAULT INITIAL SCHEMA DKE");
		}

		logger.debug("db_00_createSchema:USER");
		int dkeExists = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM INFORMATION_SCHEMA.SYSTEM_USERS WHERE USER_NAME = 'DKE'", Integer.class);
		if (dkeExists > 0) {
			jdbcTemplate.update("DROP USER DKE");

		}
		jdbcTemplate.update("CREATE USER DKE PASSWORD DKE");
		jdbcTemplate.update("ALTER USER DKE SET INITIAL SCHEMA DKE");

		logger.debug("db_00_createSchema:done");
	}

	public void db_10_raw_KREDITOR() {
		logger.debug("db_10_raw_KREDITOR");

		jdbcTemplate.update("DROP TABLE DKE.KREDITOR IF EXISTS CASCADE");
		jdbcTemplate
				.update("CREATE TEXT TABLE DKE.KREDITOR ( KredNr INTEGER PRIMARY KEY, "
						+ "Vname VARCHAR(50), Nname VARCHAR(50), Firma VARCHAR(100), PLZ VARCHAR(10), Ort VARCHAR(50), Land VARCHAR(5), "
						+ "SperrKZ VARCHAR(1), ErstellUSR VARCHAR(50), ErstellTS VARCHAR(20) NOT NULL)");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_KREDITOR:done");
	}

	public void db_10_raw_HISTORIE() {
		logger.debug("db_10_raw_HISTORIE");

		jdbcTemplate.update("DROP TABLE DKE.HISTORIE IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE TEXT TABLE DKE.HISTORIE ( AenderNr INTEGER PRIMARY KEY, "
				+ "Tabelle VARCHAR(50), Feld VARCHAR(50), ID INTEGER, Wert_alt VARCHAR(100), "
				+ "Wert_neu VARCHAR(100), AenderTS VARCHAR(20), AenderUSR VARCHAR(50))");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_HISTORIE:done");
	}

	public void db_10_raw_BESTELLUNG() {
		logger.debug("db_10_raw_BESTELLUNG");

		jdbcTemplate.update("DROP TABLE DKE.BESTELLUNG IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE TEXT TABLE DKE.BESTELLUNG ( BestellNr INTEGER PRIMARY KEY, "
				+ "KredNr INTEGER, StornoKZ VARCHAR(1), ErstellTS VARCHAR(20), ErstellUSR VARCHAR(50), "
				+ "FreigabeTS VARCHAR(20), FreigabeUSR VARCHAR(50))");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_BESTELLUNG:done");
	}

	public void db_10_raw_BESTELLPOSITION() {
		logger.debug("db_10_raw_BESTELLPOSITION");

		jdbcTemplate.update("DROP TABLE DKE.BESTELLPOSITION IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE TEXT TABLE DKE.BESTELLPOSITION ( PosNr INTEGER, BestellNr INTEGER, "
				+ "MaterialNr INTEGER, Menge INTEGER, Meinheit VARCHAR(10), Preis VARCHAR(20), Waehrung VARCHAR(10), "
				+ "StornoKZ VARCHAR(1), ErstellTS VARCHAR(20), ErstellUSR VARCHAR(50), PRIMARY KEY(PosNr, BestellNr))");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_BESTELLPOSITION:done");
	}

	public void db_10_raw_WARENEINGANG() {
		logger.debug("db_10_raw_WARENEINGANG");

		jdbcTemplate.update("DROP TABLE DKE.WARENEINGANG IF EXISTS CASCADE");
		jdbcTemplate
				.update("CREATE TEXT TABLE DKE.WARENEINGANG ( ID INTEGER PRIMARY KEY, PosNr INTEGER, BestellNr INTEGER, "
						+ "Menge INTEGER, Meinheit VARCHAR(10), EingangsTS VARCHAR(20), EingangsUSR VARCHAR(50), KredNr INTEGER)");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_WARENEINGANG:done");
	}

	public void db_10_raw_RECHNUNG() {
		logger.debug("db_10_raw_RECHNUNG");

		jdbcTemplate.update("DROP TABLE DKE.RECHNUNG IF EXISTS CASCADE");
		jdbcTemplate
				.update("CREATE TEXT TABLE DKE.RECHNUNG ( RechNr INTEGER PRIMARY KEY, PosNr INTEGER, BestellNr INTEGER, "
						+ "EingansDat VARCHAR(20), RechnungsDatum VARCHAR(20), Betrag VARCHAR(50), Waehrung VARCHAR(10), KredNr INTEGER)");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_RECHNUNG:done");
	}

	public void db_10_raw_ZAHLUNG() {
		logger.debug("db_10_raw_ZAHLUNG");

		jdbcTemplate.update("DROP TABLE DKE.ZAHLUNG IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE TEXT TABLE DKE.ZAHLUNG ( ID INTEGER PRIMARY KEY, RechNr INTEGER, "
				+ "Betrag VARCHAR(50), Waehrung VARCHAR(10), ZahlTS VARCHAR(20), ZahlUSR VARCHAR(50), KredNr INTEGER)");
		// jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_10_raw_ZAHLUNG:done");
	}

	public void db_20_extract_RAW_EVENTS() {
		logger.debug("db_20_extract_RAW_EVENTS");

		jdbcTemplate.update("DROP TABLE DKE.RAW_EVENTS IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.RAW_EVENTS ( ID INTEGER IDENTITY PRIMARY KEY, " // eindeutige
																										// ID
				+ "SOURCE VARCHAR(50), LEGACY_ID VARCHAR(100) DEFAULT NULL, " // ID
																				// des
				// Collectors,
				// ID in
				// der
				// Quelle
				+ "EVENT_TYPE VARCHAR(50) NOT NULL, EVENT_TS TIMESTAMP NOT NULL)"); // Eventdaten
		jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (EVENT_TYPE)");

		logger.debug("db_20_extract_RAW_EVENTS:done");
	}

	public void db_20_extract_RAW_EVENTS_DATA() {
		logger.debug("db_20_extract_RAW_EVENTS_DATA");

		jdbcTemplate.update("DROP TABLE DKE.RAW_EVENTS_DATA IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.RAW_EVENTS_DATA ( EVENT_ID INTEGER, " // FK
				+ "KEY VARCHAR(100) NOT NULL, VALUE VARCHAR(1000))"); // Business
																		// Daten
		// nach denen
		// gruppiert wird:
		// Key / Value
		jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_DATA_FK ON DKE.RAW_EVENTS_DATA (EVENT_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_DATA_KEY ON DKE.RAW_EVENTS_DATA (KEY)");

		logger.debug("db_20_extract_RAW_EVENTS_DATA:done");
	}

	/**
	 * Events & Co
	 */
	public void db_30_data_PROCESS() {
		logger.debug("db_30_data_PROCESS");

		jdbcTemplate.update("DROP TABLE DKE.PROCESS IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.PROCESS ( ID VARCHAR(20) PRIMARY KEY, NAME VARCHAR(100) NOT NULL)");

		logger.debug("db_30_data_PROCESS:done");
	}

	public void db_30_data_MD_EVENTS() {
		logger.debug("db_05_md_MD_EVENTS");

		jdbcTemplate.update("DROP TABLE DKE.MD_EVENTS IF EXISTS CASCADE");
//		jdbcTemplate.update("CREATE CACHED TABLE DKE.MD_EVENTS ( ID INTEGER PRIMARY KEY, NAME VARCHAR(100))");
//
//		MD_EVENTS.entrySet().forEach(
//				e -> jdbcTemplate.update("insert into DKE.MD_EVENTS values (?, ?)", e.getKey(), e.getValue()));
		jdbcTemplate.update("CREATE CACHED TABLE DKE.MD_EVENTS ( PROCESS_ID VARCHAR(20) NOT NULL, ID VARCHAR(50) NOT NULL, NAME VARCHAR(100), "
				+ " PRIMARY KEY(PROCESS_ID, ID))");
		jdbcTemplate.update("ALTER TABLE DKE.MD_EVENTS ADD FOREIGN KEY (PROCESS_ID) REFERENCES DKE.PROCESS (ID) ON DELETE CASCADE");

		logger.debug("db_05_md_MD_EVENTS:done");
	}


	public void db_30_data_CASES() {
		logger.debug("db_30_data_CASES");

		jdbcTemplate.update("DROP TABLE DKE.CASES IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.CASES ( ID INTEGER IDENTITY PRIMARY KEY, PROCESS_ID VARCHAR(20) NOT NULL, "
				+ "NAME VARCHAR(100), EVENT_ID INTEGER, IDENTIFIER VARCHAR(2000), FOOTPRINT VARCHAR(2000))");

		jdbcTemplate.update("CREATE INDEX DKE.CASES_PROCESS ON DKE.CASES (PROCESS_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.CASES_FOOTPRINT ON DKE.CASES (FOOTPRINT)");

		logger.debug("db_30_data_CASES:done");
	}

	public void db_30_data_EVENTS() {
		logger.debug("db_30_data_EVENTS");

		jdbcTemplate.update("DROP TABLE DKE.EVENTS IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.EVENTS ( ID INTEGER NOT NULL, CASE_ID INTEGER NOT NULL, "
				+ "EVENT_TYPE VARCHAR(50) NOT NULL, EVENT_TS TIMESTAMP NOT NULL, PRIMARY KEY (ID, CASE_ID))"); // Eventdaten
		jdbcTemplate.update("CREATE INDEX DKE.EVENTS_TYPE ON DKE.EVENTS (EVENT_TYPE)");

		jdbcTemplate.update("ALTER TABLE DKE.EVENTS ADD FOREIGN KEY (CASE_ID) REFERENCES DKE.CASES (ID) ON DELETE CASCADE");

		logger.debug("db_30_data_EVENTS:done");
	}

	public void db_30_data_EVENTS_DATA() {
		logger.debug("db_30_data_EVENTS_DATA");

		jdbcTemplate.update("DROP TABLE DKE.EVENTS_DATA IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.EVENTS_DATA ( EVENT_ID INTEGER, CASE_ID INTEGER NOT NULL, " // FK
				+ "KEY VARCHAR(100) NOT NULL, VALUE VARCHAR(1000))"); 
		
		jdbcTemplate.update("CREATE INDEX DKE.EVENTS_DATA_KEY ON DKE.EVENTS_DATA (KEY)");

		jdbcTemplate.update("ALTER TABLE DKE.EVENTS_DATA ADD FOREIGN KEY (EVENT_ID, CASE_ID) REFERENCES DKE.EVENTS (ID, CASE_ID) ON DELETE CASCADE");

		logger.debug("db_30_data_EVENTS_DATA:done");
	}

	public void db_30_data_MODEL() {
		logger.debug("db_30_data_MODEL");

		jdbcTemplate.update("DROP TABLE DKE.MODEL IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.MODEL ( ID INTEGER NOT NULL, PROCESS_ID VARCHAR(20) NOT NULL, "
				+ "NAME VARCHAR(100), FOOTPRINT VARCHAR(2000), "
				+ "PRIMARY KEY (ID, PROCESS_ID))");

		jdbcTemplate.update("CREATE INDEX DKE.MODEL_PROCESS ON DKE.CASES (PROCESS_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.MODEL_FOOTPRINT ON DKE.CASES (FOOTPRINT)");

		logger.debug("db_30_data_MODEL:done");
	}
	
	//
	//
	//
	// public void db_12_bpm_EVENTS() {
	// logger.debug("db_12_bpm_EVENTS");
	//
	// jdbcTemplate.update("DROP TABLE DKE.EVENTS IF EXISTS CASCADE");
	// jdbcTemplate.update("CREATE CACHED TABLE DKE.EVENTS ( PROCESS_ID INTEGER, CASE_ID INTEGER, "
	// + "EVENT_ID INTEGER, ACTIVITY VARCHAR(100) NOT NULL, ACTIVITY_TIME TIMESTAMP NOT NULL)");
	//
	// jdbcTemplate.update("CREATE INDEX DKE.EVENTS_PROCESS_ID ON DKE.EVENTS (PROCESS_ID)");
	// jdbcTemplate.update("CREATE INDEX DKE.EVENTS_CASE_ID ON DKE.EVENTS (CASE_ID)");
	// jdbcTemplate.update("CREATE INDEX DKE.EVENTS_EVENT_ID ON DKE.EVENTS (EVENT_ID)");
	// jdbcTemplate.update("CREATE INDEX DKE.EVENTS_ACTIVITY ON DKE.EVENTS (ACTIVITY)");
	//
	// logger.debug("ddb_12_bpm_EVENTS:done");
	// }

	protected void db_99_info() {
		logger.info("TABLES:");
		jdbcTemplate.queryForList("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'DKE'").forEach(
				d -> logger.info(" * {}", d));
	}

	protected void db_99_shutdown() {
		try {
			dataSource.getConnection().prepareStatement("SHUTDOWN").execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initDb() {
		logger.info("Init DB");

		db_00_createSchema();

		// Stammdaten
//		db_05_md_MD_EVENTS();

		// input daten
		db_10_raw_BESTELLPOSITION();
		db_10_raw_BESTELLUNG();
		db_10_raw_HISTORIE();
		db_10_raw_KREDITOR();
		db_10_raw_RECHNUNG();
		db_10_raw_WARENEINGANG();
		db_10_raw_ZAHLUNG();

		// Ziestruktur für extrahierte Events
		db_20_extract_RAW_EVENTS();
		db_20_extract_RAW_EVENTS_DATA();

		// Daten
		db_30_data_PROCESS();
		db_30_data_MD_EVENTS();
		db_30_data_CASES();
		db_30_data_EVENTS();
		db_30_data_EVENTS_DATA();
		db_30_data_MODEL();
		
		db_99_info();
		db_99_shutdown();

		logger.info("Init DB done");
	}

	public static void main(String[] args) {

		CreateDB2 db = new CreateDB2(Filelocations.HSQL_DB);
		db.initDb();

	}
}
