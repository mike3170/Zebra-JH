package com.stit.zebra;

import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.stit.svc",
	"com.stit.ctrl",
	"com.stit.rest",
	"com.stit.utils"}
)

/**
 * profile : dev / test / prod dev: ng serve --open test: ng build and deploy to
 * local tomcat for testing... prod: 客戶端
 */
@Import({
	WebConfig.class,
	SecurityConfProd.class,
	SecurityConfDev.class,
	TomcatConfig.class
})
public class App {

	private static Logger log = LogManager.getLogger();

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	CommandLineRunner values() {
		return args -> {
			log.info("----------------------------------------------");
			log.info("dbUrl:" + this.dbUrl);
		  log.info("Application ready...");
		};
	}

} // end class
