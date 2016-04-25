package at.jku.dke.pm.config;

import java.io.File;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

@Deprecated
public class CreateDB {

	protected static final Logger logger = LoggerFactory.getLogger(CreateDB.class);

	protected final DataSource dataSource;

	protected final JdbcTemplate jdbcTemplate;

	public CreateDB(File dbUrlFile) {
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

	public void db_01_raw_RAW_EVENTS() {
		logger.debug("db_01_raw_RAW_EVENTS");

		jdbcTemplate.update("DROP TABLE DKE.RAW_EVENTS IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.RAW_EVENTS ( ID INTEGER IDENTITY PRIMARY KEY, " // eindeutige
																										// ID
				+ "SOURCE VARCHAR(20), LEGACY_ID VARCHAR(100) DEFAULT NULL, " // ID
																				// des
				// Collectors,
				// ID in
				// der
				// Quelle
				+ "ACTIVITY VARCHAR(100) NOT NULL, ACTIVITY_TIME TIMESTAMP NOT NULL)"); // Eventdaten
		jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_ACTIVITY ON DKE.RAW_EVENTS (ACTIVITY)");

		logger.debug("db_01_raw_RAW_EVENTS:done");
	}

	public void db_01_raw_RAW_EVENTS_DATA() {
		logger.debug("db_01_raw_RAW_EVENTS_DATA");

		jdbcTemplate.update("DROP TABLE DKE.RAW_EVENTS_DATA IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.RAW_EVENTS_DATA ( EVENT_ID INTEGER, " // FK
				+ "KEY VARCHAR(100) NOT NULL, VALUE VARCHAR(1000))"); // Business
																		// Daten
		// nach denen
		// gruppiert wird:
		// Key / Value
		jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_DATA_FK ON DKE.RAW_EVENTS_DATA (EVENT_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.RAW_EVENTS_DATA_KEY ON DKE.RAW_EVENTS_DATA (KEY)");

		logger.debug("db_01_raw_RAW_EVENTS_DATA:done");
	}

	public void db_10_bpm_PROCESS() {
		logger.debug("db_10_bpm_PROCESS");

		jdbcTemplate.update("DROP TABLE DKE.PROCESS IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.PROCESS ( ID INTEGER PRIMARY KEY, NAME VARCHAR(100) NOT NULL)"); 

		logger.debug("db_10_bpm_PROCESS:done");
	}

	public void db_11_bpm_CASES() {
		logger.debug("db_11_bpm_CASES");

		jdbcTemplate.update("DROP TABLE DKE.CASES IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.CASES ( ID INTEGER NOT NULL, PROCESS_ID INTEGER NOT NULL, "
				+ "NAME VARCHAR(100), IDENTIFIER VARCHAR(2000), FOOTPRINT VARCHAR(2000), "
				+ "PRIMARY KEY (ID, PROCESS_ID))"); 

		jdbcTemplate.update("CREATE INDEX DKE.CASES_PROCESS ON DKE.CASES (PROCESS_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.CASES_FOOTPRINT ON DKE.CASES (FOOTPRINT)");

		logger.debug("db_11_bpm_CASES:done");
	}

	public void db_12_bpm_EVENTS() {
		logger.debug("db_12_bpm_EVENTS");

		jdbcTemplate.update("DROP TABLE DKE.EVENTS IF EXISTS CASCADE");
		jdbcTemplate.update("CREATE CACHED TABLE DKE.EVENTS ( PROCESS_ID INTEGER, CASE_ID INTEGER, " 
				+ "EVENT_ID INTEGER, ACTIVITY VARCHAR(100) NOT NULL, ACTIVITY_TIME TIMESTAMP NOT NULL)");
		
		jdbcTemplate.update("CREATE INDEX DKE.EVENTS_PROCESS_ID ON DKE.EVENTS (PROCESS_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.EVENTS_CASE_ID ON DKE.EVENTS (CASE_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.EVENTS_EVENT_ID ON DKE.EVENTS (EVENT_ID)");
		jdbcTemplate.update("CREATE INDEX DKE.EVENTS_ACTIVITY ON DKE.EVENTS (ACTIVITY)");

		logger.debug("ddb_12_bpm_EVENTS:done");
	}

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

		// gesammelte Events
		db_01_raw_RAW_EVENTS();
		db_01_raw_RAW_EVENTS_DATA();

		// cases
		db_10_bpm_PROCESS();
		db_11_bpm_CASES();
		db_12_bpm_EVENTS();
		
		db_99_info();
		db_99_shutdown();

		logger.info("Init DB done");
	}

	//nur no zum nachschaun ...
//	public static void main(String[] args) {
//
////		CreateDB db = new CreateDB(Filelocations.HSQL_DB);
////		db.initDb();
//
//	}
}
