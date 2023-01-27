package com.sapiens.debezium;

import org.dozer.DozerBeanMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SSIDebeziumApplication {

	public static void main(String[] args) {
		SpringApplication.run(SSIDebeziumApplication.class, args);
	}
	
	@Bean
	public DozerBeanMapper mapper() {
		return new DozerBeanMapper();
	}
}
