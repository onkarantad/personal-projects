package com.sapiens.debezium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sapiens.debezium.entity.EMPLOYEE;

@Repository
public interface EMPLOYEE_Repository extends JpaRepository<EMPLOYEE, Long>{

}
