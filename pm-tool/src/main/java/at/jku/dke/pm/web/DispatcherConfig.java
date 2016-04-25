package at.jku.dke.pm.web;


import nz.net.ultraq.thymeleaf.LayoutDialect;

import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
// alle Web Controller laden
@ComponentScan("at.jku.dke.pm.web.controller")
@EnableWebMvc
public class DispatcherConfig extends WebMvcAutoConfigurationAdapter {


	//@Value("${web.templates.cache:false}")
	protected boolean cacheTemplates = false;
	
	/* Thymeleaf Views */
	@Bean
	public TemplateResolver templateResolver() {
		ClassLoaderTemplateResolver tr = new ClassLoaderTemplateResolver();
		tr.setPrefix("web/");
		tr.setSuffix(".html");
		tr.setTemplateMode("HTML5");
		// TODO caching ist disabled
		tr.setCacheable(cacheTemplates);
		return tr;
	}
	
	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}
	
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine te = new SpringTemplateEngine();
		te.setTemplateResolver(templateResolver());
		te.addDialect(layoutDialect());
		return te;
	}

	/* ViewResolver */
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver vr = new ThymeleafViewResolver();
		vr.setTemplateEngine(templateEngine());
		vr.setCharacterEncoding("UTF-8");
		vr.setContentType("text/html");
		// vr.setViewNames(new String[]{ "*.html" });
		return vr;
	}

	/* locale */
	@Bean
	public LocaleResolver localeResolver() {
		return new SessionLocaleResolver();
	}
	
	@Bean
	public WebContentInterceptor webContentInterceptor() {
		WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
		webContentInterceptor.setCacheSeconds(0);
		webContentInterceptor.setUseCacheControlHeader(true);
		webContentInterceptor.setUseCacheControlNoStore(true);
		webContentInterceptor.setUseExpiresHeader(true);
		//webContentInterceptor.setCacheMappings(cacheMappings); // set custom caching times for specific controller actions
		return webContentInterceptor;
	}
	
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("locale");
		return localeChangeInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);
		registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(webContentInterceptor());
	}

	/**
	 * Statische Ressourcen
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
		registry.addResourceHandler("/html/**").addResourceLocations("classpath:/static/html/");
	}

}
