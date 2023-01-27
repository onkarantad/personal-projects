package com.sapiens.debezium.service;

import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sapiens.debezium.entity.AC_ACCOUNT;
import com.sapiens.debezium.repository.AC_ACCOUNT_Repository;

import io.debezium.data.Envelope.Operation;

@Service

public class AC_ACCOUNT_Service {

	
	@Autowired
	DozerBeanMapper mapper;

	private final AC_ACCOUNT_Repository repository;

	public AC_ACCOUNT_Service(AC_ACCOUNT_Repository repository) {
		this.repository = repository;
	}

	public void replicateData(Map<String, Object> data, Operation operation) {
//		final ObjectMapper mapper = new ObjectMapper();
//		final DozerBeanMapper mapper = new DozerBeanMapper();
		final AC_ACCOUNT obj = mapper.map(data, AC_ACCOUNT.class);
//		log.info("payload obj :-> "+data);
//		log.info("AC_ACCOUNT obj :-> "+obj);

		if (Operation.DELETE == operation) {
			repository.deleteById(obj.getID());
		} else {
			repository.save(obj);
		}
	}

}
