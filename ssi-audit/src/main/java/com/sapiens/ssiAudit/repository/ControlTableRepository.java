package com.sapiens.ssiAudit.repository;

import com.sapiens.ssiAudit.model.ControlTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

public interface ControlTableRepository extends JpaRepository<ControlTable,Integer> {

    @Transactional
    default ControlTable persist(EntityManager entityManager,ControlTable controlTable){
        entityManager.persist(controlTable);
        return controlTable;
    }
}
