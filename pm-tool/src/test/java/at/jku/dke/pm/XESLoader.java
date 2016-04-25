package at.jku.dke.pm;

import static org.joox.JOOX.$;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import at.jku.dke.pm.config.Filelocations;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XESLoader.class)
@Configuration
public class XESLoader {

	protected static final Logger logger = LoggerFactory.getLogger(XESLoader.class);

	private final static File ROOT = new File("C:\\appl\\ws\\LB-SSC\\pm-tool\\src\\test\\resources\\xes");

	public static final String PROCESS_REVIEW = "review";
	public static final String PROCESS_TELECLAIMS = "teleclaims";
	
	@Bean
	public DataSource dataSource() {
		String dbUrl = String.format("jdbc:hsqldb:file:%s;hsqldb.script_format=3", Filelocations.HSQL_DB.getAbsolutePath());

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
		loadXES("repair", new File(ROOT, "repairExample.xes"));
	}

	@Test
	public void loadRepairExample2() throws Exception {
		loadXES("repair2", new File(ROOT, "repairExampleSample2.xes"));
	}

	@Test
	public void loadTeleclaims() throws Exception {
		loadXES(PROCESS_TELECLAIMS, new File(ROOT, "teleclaims.xes"));
	}

	public void loadXES(String processId, File xesFile) throws Exception {
		Document doc = JOOX.builder().parse(xesFile);

		$(doc).find("trace").forEach(
				ctx -> {
					int caseId = addCase(processId, $(ctx).children().matchAttr("key", NAME).attr("value"));
					AtomicInteger idx = new AtomicInteger(0);
					$(ctx).children("event").forEach(
							eCtx -> {
								addEvent( caseId, idx.getAndIncrement(), $(eCtx).children().matchAttr("key", NAME).attr("value"),
										LocalDateTime.parse($(eCtx).children().matchAttr("key", TIME).attr("value"),
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
		jdbcTemplate.update("insert into EVENTS( CASE_ID, ID, EVENT_TYPE, EVENT_TS) values ?, ?, ?, ?",
				caseId, id, eventId, Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));

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
		jdbcTemplate.queryForList("select * from EVENTS").forEach(d -> logger.debug("EVENTS: {}", d));
	}

}
