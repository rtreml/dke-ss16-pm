package at.jku.dke.pm;

import java.io.File;
import java.nio.file.Files;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.ProcessRepository;
import at.jku.dke.pm.services.ProcessService;
import at.jku.dke.pm.services.impl.SimpleProcessService;
import at.jku.dke.pm.services.repositories.JdbcCaseRepository;
import at.jku.dke.pm.services.repositories.JdbcProcessRepository;

@Configuration
@EnableAutoConfiguration()
// exclude = { ThymeleafAutoConfiguration.class })
public class ApplicationConfig {

	protected static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

	// @Bean
	// public EmbeddedServletContainerFactory servletContainer() {
	// TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
	// tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
	//
	// @Override
	// public void customize(Connector connector) {
	// connector.setMaxPostSize(0);
	// connector.setAsyncTimeout(10 * 60 * 1000);
	// }
	// });
	// return tomcat;
	// }

	@Value("${DB:null}")
	private String dbPath;

	@Bean
	public DataSource dataSource() {
		logger.info("DB: {}", dbPath);

		File hsqlDB = null;
		if (dbPath != null) {
			hsqlDB = new File(dbPath);
		}

		if (hsqlDB == null || !hsqlDB.isDirectory()) {
			hsqlDB = Filelocations.HSQL_DB;
		} else {
			hsqlDB = new File(dbPath, "pdm.hsql");
		}

		String dbUrl = String.format("jdbc:hsqldb:file:%s;hsqldb.script_format=3", hsqlDB.getAbsolutePath());
		logger.info("DBURL: {}", dbUrl);

		return DataSourceBuilder.create().driverClassName("org.hsqldb.jdbcDriver").url(dbUrl).username("SA")
				.password("").build();
	}

	@Bean
	public CaseRepository caseRepository() {
		return new JdbcCaseRepository(dataSource());
	}

	@Bean
	public ProcessRepository processRepository() {
		return new JdbcProcessRepository(dataSource());
	}

	@Bean
	public ProcessService processService() {
		return new SimpleProcessService();
	}
}
