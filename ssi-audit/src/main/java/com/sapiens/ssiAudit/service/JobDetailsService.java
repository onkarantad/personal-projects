package com.sapiens.ssiAudit.service;

import com.sapiens.ssiAudit.model.JobDetails;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface JobDetailsService {
    JobDetails saveOrUpdate(JobDetails jobDetails);
    CompletableFuture<JobDetails> saveOrUpdateAsync(JobDetails jobDetails);
    Integer updateByBatchIdSubBatchId(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc);
    CompletableFuture<Integer> updateByBatchIdSubBatchIdAsync(int JOB_NUM, int BATCH_ID, int SUB_BATCH_ID, Timestamp CORE_DB_BATCH_END_DATE, int STATUS_NUM, Timestamp CURRENT_BATCH_END_DATE, String Failure_Point, Integer Technical_Error_Code, String Status_Desc, String Technical_Error_Desc);
    List<Object> findBatchIdSubBatchID(String Target_Audit_Schema_Nm, int JOB_NUM);
    CompletableFuture<List<Object>> findBatchIdSubBatchIDAsync(String Target_Audit_Schema_Nm, int JOB_NUM);
}
