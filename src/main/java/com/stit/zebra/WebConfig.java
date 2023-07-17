package com.stit.zebra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
//import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import java.util.List;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.data.domain.AuditorAware;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.util.UrlPathHelper;
//import org.thymeleaf.spring4.SpringTemplateEngine;
//import org.thymeleaf.spring4.view.ThymeleafViewResolver;
//import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

	private Logger log = LogManager.getLogger();

	/**
	 * 重要, especial for http DELETE method, deafult is not allow. CORS = cross
	 * orgin resource sharing
	 *
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins("*")
			.allowedHeaders("*")
			.allowedMethods("*");
	}

	/**
	 * JPA @createBy....
	 *
	 * @return
	 */
	//@Bean
	//public AuditorAware<String> auditorProvider() {
	//	return new AuditAwareImpl();
	//}

	/**
	 * matrix variables
	 *
	 * @param configurer
	 */
	@Override
	protected void configurePathMatch(PathMatchConfigurer configurer) {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		urlPathHelper.setRemoveSemicolonContent(false);
		configurer.setUrlPathHelper(urlPathHelper);
		configurer.setUseSuffixPatternMatch(false);  // attn Date ISO  xxxx.xxxZ
	}

	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(mappingJackson2HttpMessageConverter());
		converters.add(new StringHttpMessageConverter());

		super.configureMessageConverters(converters);
	}

	/**
	 * resolve for JPA inifinite loop one2many jackson hibernate type
	 * jackson-datatype-hibernate5
	 *
	 * @return
	 */
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = jsonConverter.getObjectMapper();

		// ---------------------------------
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));

	ISO8601DateFormat df = new ISO8601DateFormat();
	df.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
	//objectMapper.setDateFormat(df);

		// david for java.sql.Date -- ATTN
		// because DateFormat not used for java.sql.Date
		//SimpleModule myModule = new SimpleModule();
		//myModule.addSerializer(java.sql.Date.class, new DateSerializer());
		//objectMapper.registerModule(myModule);

		//objectMapper.registerModule(new Hibernate5Module());

		//-----------------------------------------------
		jsonConverter.setObjectMapper(objectMapper);
		jsonConverter.setPrettyPrint(true);

		return jsonConverter;
	}

	// thymeleaf, using auto config feature 
	/*
	@Bean
	public ClassLoaderTemplateResolver templateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setPrefix("templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setCacheable(false);
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setCharacterEncoding("UTF-8");
		//templateResolver.setOrder(1); //david

		return templateResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;

	}

	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();

		viewResolver.setTemplateEngine(templateEngine());
		viewResolver.setCharacterEncoding("UTF-8");

		return viewResolver;
	}
	 */
	/**
	 * index.html, redirect(必須是重新導向 redirect) login.html
	 * @param registry
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "index.html");
		//registry.addViewController("/login/form").setViewName("login");
	}

	/**
	 * Angular 5, static content index.html loaded, then *.js, *.css loading,
	 * instruct it to /static/
	 * @param registry
	 */
	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
			.addResourceLocations(
				"classpath:/public/",
				"classpath:/static/",
				"classpath:/static/css",
				"classpath:/static/js",
				"classpath:/templates/",
				"classpath:/keys/");
	}

} // end class
