package at.jku.dke.pm;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.jku.dke.pm.config.Filelocations;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.ModelRepository;
import at.jku.dke.pm.services.repositories.JdbcCaseRepository;
import at.jku.dke.pm.services.repositories.JdbcModelRepository;

@Configuration
@EnableAutoConfiguration()//exclude = { ThymeleafAutoConfiguration.class })
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

	@Bean
	public DataSource dataSource() {
		String dbUrl = String.format("jdbc:hsqldb:file:%s;hsqldb.script_format=3",
				Filelocations.HSQL_DB.getAbsolutePath());

		return DataSourceBuilder.create().driverClassName("org.hsqldb.jdbcDriver").url(dbUrl).username("SA")
				.password("").build();
	}

	@Bean
	public CaseRepository caseRepository() {
		return new JdbcCaseRepository(dataSource());
	}

	@Bean
	public ModelRepository modelRepository() {
		return new JdbcModelRepository(dataSource());
	}

}
