package com.sapiens.ssiAudit.repository;

import com.sapiens.ssiAudit.model.JobDetailsHistory;
import com.sapiens.ssiAudit.constants.SSIAppConstants;
import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.sql.Timestamp;

public interface JobDetailsHistoryRepository extends JpaRepository<JobDetailsHistory, Integer> {

    // update job details history
    @Transactional
    default Integer updateJobDetailsHistoryByBatchIdSubBatchId(EntityManager entityManager, String nativeQuery, int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc) {
        Query query = entityManager.createNativeQuery(nativeQuery);
        query.setParameter(SSIAppConstants.JOB_NUM, JOB_NUM);
        query.setParameter(SSIAppConstants.BATCH_ID, BATCH_ID);
        query.setParameter(SSIAppConstants.SUB_BATCH_ID, SUB_BATCH_ID);
        query.setParameter(SSIAppConstants.CORE_DB_BATCH_END_DATE, CORE_DB_BATCH_END_DATE, TemporalType.TIMESTAMP);
        query.setParameter(SSIAppConstants.STATUS_NUM, STATUS_NUM);
        query.setParameter(SSIAppConstants.CURRENT_BATCH_END_DATE, CURRENT_BATCH_END_DATE, TemporalType.TIMESTAMP);
        query.setParameter(SSIAppConstants.Failure_Point, Failure_Point);
        query.setParameter(SSIAppConstants.Technical_Error_Code, new TypedParameterValue(StandardBasicTypes.INTEGER, Technical_Error_Code));
        query.setParameter(SSIAppConstants.Status_Desc, Status_Desc);
        query.setParameter(SSIAppConstants.Technical_Error_Desc, Technical_Error_Desc);
        return query.executeUpdate();
    }
}
