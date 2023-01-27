package com.sapiens.ssiAudit.service.impl;

import com.sapiens.ssiAudit.model.JobDetails;
import com.sapiens.ssiAudit.repository.JobDetailsRepository;
import com.sapiens.ssiAudit.service.JobDetailsService;
import com.sapiens.ssiAudit.service.dbService.DBService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class JobDetailsImpl implements JobDetailsService {

    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    @Autowired
    DBService dbService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public JobDetails saveOrUpdate(JobDetails jobDetails) {
        log.trace("Looking up " + jobDetails);
        return jobDetailsRepository.save(jobDetails);
    }

    @Override
    @Async
    public CompletableFuture<JobDetails> saveOrUpdateAsync(JobDetails jobDetails) {
        log.trace("Looking up " + jobDetails);
        return CompletableFuture.completedFuture(jobDetailsRepository.save(jobDetails));
    }


    @Override
    public Integer updateByBatchIdSubBatchId(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc) {
        return jobDetailsRepository.updateJobDetailsByBatchIdSubBatchId(entityManager, dbService.mjUpdateJobDetailsQueryJPA(), JOB_NUM, BATCH_ID, SUB_BATCH_ID, CORE_DB_BATCH_END_DATE, STATUS_NUM, CURRENT_BATCH_END_DATE, Failure_Point, Technical_Error_Code, Status_Desc, Technical_Error_Desc);

    }

    @Override
    @Async
    public CompletableFuture<Integer> updateByBatchIdSubBatchIdAsync(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc) {
        return CompletableFuture.completedFuture(jobDetailsRepository.updateJobDetailsByBatchIdSubBatchId(entityManager, dbService.mjUpdateJobDetailsQueryJPA(), JOB_NUM, BATCH_ID, SUB_BATCH_ID, CORE_DB_BATCH_END_DATE, STATUS_NUM, CURRENT_BATCH_END_DATE, Failure_Point, Technical_Error_Code, Status_Desc, Technical_Error_Desc));

    }

    @Override
    public List<Object> findBatchIdSubBatchID(String Target_Audit_Schema_Nm, int JOB_NUM) {
        List<Object> obj = jobDetailsRepository.findBatchIdSubBatchId(entityManager, dbService.mjCombineJobDetailsparamsQueryJPA(), JOB_NUM);
        if (obj.isEmpty()) {
            Object[] objArr = {1, 1, null, null};
            obj.add(objArr);
        }
        return obj;
    }


    @Override
    @Async
    public CompletableFuture<List<Object>> findBatchIdSubBatchIDAsync(String Target_Audit_Schema_Nm, int JOB_NUM) {
        List<Object> obj = jobDetailsRepository.findBatchIdSubBatchId(entityManager, dbService.mjCombineJobDetailsparamsQueryJPA(), JOB_NUM);
        if (obj.isEmpty()) {
            Object[] objArr = {1, 1, null, null};
            obj.add(objArr);
        }
        return CompletableFuture.completedFuture(obj);
    }


}
