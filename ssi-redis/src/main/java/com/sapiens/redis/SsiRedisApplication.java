package com.sapiens.redis;



import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
#Redis Main Application 
This service is built to store Dedup value and master data lookup in-memory &
on calling the api with valid input fetches the respective data from redis in-memory
*/

@SpringBootApplication
@Log4j2
public class SsiRedisApplication{
	public static void main(String[] args) {
		log.info("SsiRedis Application Started");
//		SpringApplication application = new SpringApplication(SsiRedisApplication.class);
//		application.addListeners(new LoggingListener());
//		application.run(args);
		SpringApplication.run(SsiRedisApplication.class, args);

	}
}
