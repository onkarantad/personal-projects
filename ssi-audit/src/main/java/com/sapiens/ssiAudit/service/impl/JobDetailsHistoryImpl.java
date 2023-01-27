package com.sapiens.ssiAudit.service.impl;

import com.sapiens.ssiAudit.model.JobDetailsHistory;
import com.sapiens.ssiAudit.repository.JobDetailsHistoryRepository;
import com.sapiens.ssiAudit.service.JobDetailsHistoryService;
import com.sapiens.ssiAudit.service.dbService.DBService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class JobDetailsHistoryImpl implements JobDetailsHistoryService {

    @Autowired
    private JobDetailsHistoryRepository jobDetailsHistoryRepository;

    @Autowired
    DBService dbService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public JobDetailsHistory saveOrUpdate(JobDetailsHistory jobDetailsHistory) {
        log.trace("Looking up " + jobDetailsHistory);
        return jobDetailsHistoryRepository.save(jobDetailsHistory);
    }

    @Override
    @Async
    public CompletableFuture<JobDetailsHistory> saveOrUpdateAsync(JobDetailsHistory jobDetailsHistory) {
        log.trace("Looking up " + jobDetailsHistory);
        return CompletableFuture.completedFuture(jobDetailsHistoryRepository.save(jobDetailsHistory));
    }

    @Override
    public Integer updateByBatchIdSubBatchId(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc) {
        return jobDetailsHistoryRepository.updateJobDetailsHistoryByBatchIdSubBatchId(entityManager, dbService.mjUpdateJobDetailsHistoryQueryJPA(), JOB_NUM, BATCH_ID, SUB_BATCH_ID, CORE_DB_BATCH_END_DATE, STATUS_NUM, CURRENT_BATCH_END_DATE, Failure_Point, Technical_Error_Code, Status_Desc, Technical_Error_Desc);
    }

    @Override
    @Async
    public CompletableFuture<Integer> updateByBatchIdSubBatchIdAsync(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc) {
        return CompletableFuture.completedFuture(jobDetailsHistoryRepository.updateJobDetailsHistoryByBatchIdSubBatchId(entityManager, dbService.mjUpdateJobDetailsHistoryQueryJPA(), JOB_NUM, BATCH_ID, SUB_BATCH_ID, CORE_DB_BATCH_END_DATE, STATUS_NUM, CURRENT_BATCH_END_DATE, Failure_Point, Technical_Error_Code, Status_Desc, Technical_Error_Desc));
    }

}
