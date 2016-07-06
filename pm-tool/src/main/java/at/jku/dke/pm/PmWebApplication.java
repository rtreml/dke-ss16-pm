package at.jku.dke.pm;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import at.jku.dke.pm.web.DispatcherConfig;

public class PmWebApplication {

	protected static final Logger logger = LoggerFactory.getLogger(PmWebApplication.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("args: "+ Arrays.stream( args ).collect(Collectors.joining(", ")) );
        SpringApplication.run(new Object[] {DispatcherConfig.class, ApplicationConfig.class}, args);
	}

}
