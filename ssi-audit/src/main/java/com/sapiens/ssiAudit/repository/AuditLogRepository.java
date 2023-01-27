package com.sapiens.ssiAudit.repository;

import com.sapiens.ssiAudit.model.AuditLog;
import com.sapiens.ssiAudit.constants.SSIAppConstants;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    // find business start date (max of business end date from audit log)
    default List<Object> findBusinessStartDate(EntityManager entityManager, String nativeQuery, int WF_NUM) {
        Query query = entityManager.createNativeQuery(nativeQuery);
        query.setParameter(SSIAppConstants.WF_NUM, WF_NUM);
        return query.getResultList();
    }
}
