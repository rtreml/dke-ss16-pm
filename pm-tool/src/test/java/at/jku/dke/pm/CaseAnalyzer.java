package at.jku.dke.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.jku.dke.pm.collect.BestellPosCaseCollector;
import at.jku.dke.pm.config.Filelocations;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CaseAnalyzer.class)
@Configuration
public class CaseAnalyzer {

	protected static final Logger logger = LoggerFactory.getLogger(CaseAnalyzer.class);

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

	// protected void addEvent(int processId, int caseId, int eventId, String activity, LocalDateTime time) {
	// logger.debug("EVENT: {} {}", activity, time);
	// jdbcTemplate.update("insert into EVENTS( CASE_ID, PROCESS_ID, ACTIVITY, ACTIVITY_TIME) values ?, ?, ?, ?",
	// caseId, processId, activity, Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
	//
	// }

//	@Test
//	public void generateFootprint() {
//		int processId = 2;
//		jdbcTemplate.queryForList(
//				"select count(*) || '|' || group_concat(activity order by activity_time ASC separator '|') from EVENTS "
//						+ " where PROCESS_ID = ? and CASE_ID = ? group by PROCESS_ID, CASE_ID", processId, 1).forEach(
//				d -> logger.debug("FOOTPRINT: {}", d));
//
//		logger.debug(
//				"update: {}",
//				jdbcTemplate
//						.update("update CASES c set FOOTPRINT = (select count(*) || '|' || group_concat(activity order by activity_time ASC separator '|') from EVENTS e "
//								+ " where e.PROCESS_ID = c.PROCESS_ID and e.CASE_ID = c.ID group by PROCESS_ID, CASE_ID) where c.PROCESS_ID = ?",
//								processId));
//		// jdbcTemplate.update("truncate table CASES AND COMMIT");
//	}
//

	@Test
	public void generateFootprint() {
		String processId = XESLoader.PROCESS_TELECLAIMS;
		processId = BestellPosCaseCollector.PROCESS_ID;
		
//		jdbcTemplate.queryForList(
//				"select count(*) || '|' || group_concat(EVENT_TYPE order by EVENT_TS ASC separator '|') from EVENTS "
//						+ " where CASE_ID = ? group by CASE_ID", 10).forEach(
//				d -> logger.debug("FOOTPRINT: {}", d));

		logger.debug(
				"update: {}",
				jdbcTemplate
						.update("update CASES c set FOOTPRINT = (select lpad(count(*),4,'0') || '|' || group_concat(EVENT_TYPE order by EVENT_TS ASC separator '|') from EVENTS e "
								+ " where e.CASE_ID = c.ID group by CASE_ID) where c.PROCESS_ID = ?",
								processId));
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

	public void dumpDataCvs(List<Map<String, Object>> data) {

		List<String> columns = new ArrayList<String>(data.get(0).keySet());

		StringBuffer res = new StringBuffer();
		res.append(columns.stream().collect(Collectors.joining(", "))).append('\n');

		for (Map<String, Object> line : data) {
			res.append(
					columns.stream().map(c -> line.get(c)).map(o -> o != null ? o : "").map(Object::toString).map(s -> "'"+s+"'")
							.collect(Collectors.joining(","))).append('\n');
		}
		logger.debug("DATA:\n{}", res.toString());

	}

}
