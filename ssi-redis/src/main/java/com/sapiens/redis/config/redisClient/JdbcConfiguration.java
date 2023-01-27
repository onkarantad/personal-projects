package com.sapiens.redis.config.redisClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;


/*
 * The Class JdbcConfiguration.
 *
 **/
@Configuration
public class JdbcConfiguration {
	/*
	 * get datasource from config.
	 *
	 **/
	@Autowired
	private DataSource dataSource;

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
		NamedParameterJdbcTemplate dbBean = new NamedParameterJdbcTemplate(dataSource);
		return dbBean;
	}

	@Bean
	public DataSourceTransactionManager txnManager() {
		DataSourceTransactionManager txnManager = new DataSourceTransactionManager(dataSource);
		return txnManager;
	}
}
