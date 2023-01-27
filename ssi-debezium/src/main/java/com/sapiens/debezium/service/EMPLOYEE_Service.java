package com.sapiens.debezium.service;

import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Service;

import com.sapiens.debezium.entity.EMPLOYEE;
import com.sapiens.debezium.repository.EMPLOYEE_Repository;

import io.debezium.data.Envelope.Operation;

@Service
public class EMPLOYEE_Service {

	private final EMPLOYEE_Repository employeeRepository;

	public EMPLOYEE_Service(EMPLOYEE_Repository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public void replicateData(Map<String, Object> data, Operation operation) {
//		final ObjectMapper mapper = new ObjectMapper();
		final DozerBeanMapper mapper = new DozerBeanMapper();
		final EMPLOYEE employee = mapper.map(data, EMPLOYEE.class);
		System.out.println("payload obj :-> "+data);
		System.out.println("employee obj :-> "+employee);

		if (Operation.DELETE == operation) {
			employeeRepository.deleteById(employee.getID());
		} else {
			employeeRepository.save(employee);
		}
	}

}
