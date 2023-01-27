package com.sapiens.debezium.service;

import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Service;

import com.sapiens.debezium.entity.AC_GL_BALANCE;
import com.sapiens.debezium.repository.AC_GL_BALANCE_Repository;

import io.debezium.data.Envelope.Operation;

@Service
public class AC_GL_BALANCE_Service {
	private final AC_GL_BALANCE_Repository repository;

	public AC_GL_BALANCE_Service(AC_GL_BALANCE_Repository repository) {
		this.repository = repository;
	}

	public void replicateData(Map<String, Object> data, Operation operation) {
//		final ObjectMapper mapper = new ObjectMapper();
		final DozerBeanMapper mapper = new DozerBeanMapper();
		final AC_GL_BALANCE obj = mapper.map(data, AC_GL_BALANCE.class);
		System.out.println("payload obj before:-> " + data);
		System.out.println("AC_GL_BALANCE obj :-> " + obj);

		if (Operation.DELETE == operation) {
			repository.deleteById(obj.getID());
		} else {
			repository.save(obj);
		}
	}
}
