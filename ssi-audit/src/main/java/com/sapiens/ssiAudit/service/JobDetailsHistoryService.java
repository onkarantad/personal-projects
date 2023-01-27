package com.sapiens.ssiAudit.service;

import com.sapiens.ssiAudit.model.JobDetailsHistory;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

public interface JobDetailsHistoryService {
    JobDetailsHistory saveOrUpdate(JobDetailsHistory jobDetailsHistory);
    CompletableFuture<JobDetailsHistory> saveOrUpdateAsync(JobDetailsHistory jobDetailsHistory);
    Integer updateByBatchIdSubBatchId(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc);
    CompletableFuture<Integer> updateByBatchIdSubBatchIdAsync(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc);

}
