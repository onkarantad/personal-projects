package com.sapiens.ssi.connection.nosql;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoConfigurationException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.log4j.Log4j2;


import java.util.concurrent.TimeUnit;

@Log4j2
public class MongoConnectionPool {
	static MongoClient mongoClient = null;

	public static MongoClient getConnection(String uriTarget) {

		try {
			log.info("Providing mongodb connection");

			if (mongoClient == null) {
				try {
					log.info("Creating mongodb connection pool");

					mongoClient = MongoClients.create(MongoClientSettings.builder()
							.applyConnectionString(new ConnectionString(uriTarget)).applyToConnectionPoolSettings(
									builder -> builder.maxWaitTime(10, TimeUnit.SECONDS).maxSize(200))
							.build());

				} catch (MongoConfigurationException e) {
					log.fatal("Mongodb Target connection not established - " + e);

				}
			}

		} catch (Exception e) {
			log.fatal("<" + MongoConnectionPool.class.getSimpleName() + "> - Connection Pool failed to establish connection with database - "
					+ e);

		}
		return mongoClient;
	}

	public static void closeConnection() {
		mongoClient.close();
	}

}
