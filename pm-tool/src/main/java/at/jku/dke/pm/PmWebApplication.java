package at.jku.dke.pm;

import org.springframework.boot.SpringApplication;

import at.jku.dke.pm.web.DispatcherConfig;

public class PmWebApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        SpringApplication.run(new Object[] {DispatcherConfig.class, ApplicationConfig.class}, args);
	}

}
