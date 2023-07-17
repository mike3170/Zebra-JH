package com.stit.zebra;

import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "prod"})
public class SecurityConfProd {

	private final Logger log = LogManager.getLogger();

	@Autowired
	private DataSource ds;


} // end classs
