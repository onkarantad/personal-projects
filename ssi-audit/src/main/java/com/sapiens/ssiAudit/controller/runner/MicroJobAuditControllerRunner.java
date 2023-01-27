package com.sapiens.ssiAudit.controller.runner;

import com.sapiens.ssiAudit.constants.SSIAppConstants;
import com.sapiens.ssiAudit.model.AuditLog;
import com.sapiens.ssiAudit.model.ControlTable;
import com.sapiens.ssiAudit.model.JobDetails;
import com.sapiens.ssiAudit.model.JobDetailsHistory;
import com.sapiens.ssiAudit.service.AuditLogService;
import com.sapiens.ssiAudit.service.ControlTableService;
import com.sapiens.ssiAudit.service.JobDetailsHistoryService;
import com.sapiens.ssiAudit.service.JobDetailsService;
import com.sapiens.ssiAudit.service.dbService.DBService;
import com.sapiens.ssiAudit.util.CommonUtils;
import com.sapiens.ssiAudit.util.RealTimeAudit;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Log4j2
@Component
public class MicroJobAuditControllerRunner {

    @Autowired
    DBService dbService;

    @Autowired
    JobDetailsService jobDetailsService;

    @Autowired
    JobDetailsHistoryService jobDetailsHistoryService;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    ControlTableService controlTableService;

    @Autowired
    CommonUtils commonUtils;

    @Autowired
    private DataSource dataSource;

    /*
    Payload Keys -

    Target_Audit_Schema_Nm: DB default schema name
    JOB_NUM: job number
    Source_Zone_Id: to fetch source DB timestamp
    STATUS_NUM: status
    CURRENT_BATCH_START_DATE: batch start date
    CURRENT_BATCH_END_DATE: batch end date
    CORE_DB_BATCH_START_DATE: db start date
    CORE_DB_BATCH_END_DATE: db end date
    TABLE_NAME: table name
    WF_NUM: table number
    BATCH_ID: batch id
    SUB_BATCH_ID: sub batch id
    STG_CNT: source count
    MIS_CNT: target count
    Failure_Point: where failure happened
    Technical_Error_Code: code of error
    Technical_Error_Desc: error description
    */

    // JOB LEVEL pre scripting
    public List<Map<String, Object>> preScriptRunAsync(Map<String, Object> payload) {

        String Source_Zone_Id = (String) payload.get(SSIAppConstants.Source_Zone_Id);
        Object CORE_DB_BATCH_START_DATE = commonUtils.getTimestampByZoneID(Source_Zone_Id);
        String Target_Audit_Schema_Nm = (String) payload.get(SSIAppConstants.Target_Audit_Schema_Nm);
        int JOB_NUM = (int) payload.get(SSIAppConstants.JOB_NUM);
        Object CURRENT_BATCH_START_DATE = payload.get(SSIAppConstants.CURRENT_BATCH_START_DATE);
        int STATUS_NUM = (int) payload.get(SSIAppConstants.STATUS_NUM);

        List<Object> batchIdSubBatchId = jobDetailsService.findBatchIdSubBatchID(Target_Audit_Schema_Nm, JOB_NUM);
        int BATCH_ID = (int) ((Object[]) batchIdSubBatchId.get(0))[0];
        log.trace("BATCH_ID: " + BATCH_ID);
        int SUB_BATCH_ID = (int) ((Object[]) batchIdSubBatchId.get(0))[1];
        log.trace("SUB_BATCH_ID: " + SUB_BATCH_ID);
        Object PREVIOUS_BATCH_START_DATE = ((Object[]) batchIdSubBatchId.get(0))[2];
        log.trace("PREVIOUS_BATCH_START_DATE: " + PREVIOUS_BATCH_START_DATE);
        Object PREVIOUS_BATCH_END_DATE = ((Object[]) batchIdSubBatchId.get(0))[3];
        log.trace("PREVIOUS_BATCH_END_DATE: " + PREVIOUS_BATCH_END_DATE);
        log.debug("result: " + Arrays.asList((Object[]) batchIdSubBatchId.get(0)));

        List<Map<String, Object>> batchIdSubBatchIdList = new ArrayList<>();
        Map<String, Object> batchIdSubBatchIdMap = new HashMap<>();
        batchIdSubBatchIdMap.put(SSIAppConstants.BATCH_ID, BATCH_ID);
        batchIdSubBatchIdMap.put(SSIAppConstants.SUB_BATCH_ID, SUB_BATCH_ID);
        batchIdSubBatchIdList.add(batchIdSubBatchIdMap);

        PREVIOUS_BATCH_START_DATE = PREVIOUS_BATCH_START_DATE != null ? (((Object) SSIAppConstants.Default_Date).toString().length() < 1 ? null : (PREVIOUS_BATCH_START_DATE)) : null;
        PREVIOUS_BATCH_END_DATE = PREVIOUS_BATCH_END_DATE != null ? (((Object) PREVIOUS_BATCH_END_DATE).toString().length() < 1 ? null : (PREVIOUS_BATCH_END_DATE)) : null;

        Timestamp PREVIOUS_BATCH_START_DATE_TS = PREVIOUS_BATCH_START_DATE != null ? (Timestamp.valueOf(PREVIOUS_BATCH_START_DATE.toString())) : null;
        Timestamp PREVIOUS_BATCH_END_DATE_TS = PREVIOUS_BATCH_END_DATE != null ? (Timestamp.valueOf(PREVIOUS_BATCH_END_DATE.toString())) : null;

        JobDetails jobDetails = new JobDetails(JOB_NUM, BATCH_ID, SUB_BATCH_ID, JOB_NUM, Timestamp.valueOf(CORE_DB_BATCH_START_DATE.toString()), null, Timestamp.valueOf(CURRENT_BATCH_START_DATE.toString()), null, PREVIOUS_BATCH_START_DATE_TS, PREVIOUS_BATCH_END_DATE_TS, STATUS_NUM, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""));
        log.info("jobDetailsObj: " + jobDetails);
        JobDetailsHistory jobDetailsHistory = new JobDetailsHistory(JOB_NUM, BATCH_ID, SUB_BATCH_ID, JOB_NUM, Timestamp.valueOf(CORE_DB_BATCH_START_DATE.toString()), null, Timestamp.valueOf(CURRENT_BATCH_START_DATE.toString()), null, PREVIOUS_BATCH_START_DATE_TS, PREVIOUS_BATCH_END_DATE_TS, STATUS_NUM, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""));
        log.info("jobDetailsHistoryObj: " + jobDetailsHistory);

        // persist object in db
        CompletableFuture<JobDetails> jobDetailsAsync = jobDetailsService.saveOrUpdateAsync(jobDetails);
        CompletableFuture<JobDetailsHistory> JobDetailsHistoryAsync = jobDetailsHistoryService.saveOrUpdateAsync(jobDetailsHistory);

        CompletableFuture.allOf(jobDetailsAsync, JobDetailsHistoryAsync).join();
        try {
            log.info("jobDetails saved: " + jobDetailsAsync.get());
            log.info("jobDetailsHistory saved: " + jobDetailsAsync.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        return batchIdSubBatchIdList;
    }

    // JOB LEVEL post scripting
    public ResponseEntity<String> postScriptRunAsync(Map<String, Object> payload) {
        String Source_Zone_Id = (String) payload.get(SSIAppConstants.Source_Zone_Id);
        Object CORE_DB_BATCH_END_DATE = commonUtils.getTimestampByZoneID(Source_Zone_Id);
        String Target_Audit_Schema_Nm = (String) payload.get(SSIAppConstants.Target_Audit_Schema_Nm);
        Object CURRENT_BATCH_END_DATE = payload.get(SSIAppConstants.CURRENT_BATCH_END_DATE);
        int STATUS_NUM = (int) payload.get(SSIAppConstants.STATUS_NUM);
        int JOB_NUM = (int) payload.get(SSIAppConstants.JOB_NUM);
        int BATCH_ID = (int) payload.get(SSIAppConstants.BATCH_ID);
        int SUB_BATCH_ID = (int) payload.get(SSIAppConstants.SUB_BATCH_ID);
        String Failure_Point = (String) payload.get(SSIAppConstants.Failure_Point);
        Integer Technical_Error_Code = (Integer) payload.get(SSIAppConstants.Technical_Error_Code);
        String Technical_Error_Desc = (String) payload.get(SSIAppConstants.Technical_Error_Desc);

        Timestamp CORE_DB_BATCH_END_DATE_TS = Timestamp.valueOf(CORE_DB_BATCH_END_DATE.toString());
        Timestamp CURRENT_BATCH_END_DATE_TS = Timestamp.valueOf(CURRENT_BATCH_END_DATE.toString());

        // update object in db
        CompletableFuture<Integer> jobDetailsUpdateAsync = jobDetailsService.updateByBatchIdSubBatchIdAsync(JOB_NUM, BATCH_ID, SUB_BATCH_ID, CORE_DB_BATCH_END_DATE_TS, STATUS_NUM, CURRENT_BATCH_END_DATE_TS, Failure_Point, Technical_Error_Code, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Technical_Error_Desc);
        CompletableFuture<Integer> jobDetailsHistoryUpdateAsync = jobDetailsHistoryService.updateByBatchIdSubBatchIdAsync(JOB_NUM, BATCH_ID, SUB_BATCH_ID, CORE_DB_BATCH_END_DATE_TS, STATUS_NUM, CURRENT_BATCH_END_DATE_TS, Failure_Point, Technical_Error_Code, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Technical_Error_Desc);

        CompletableFuture.allOf(jobDetailsUpdateAsync, jobDetailsHistoryUpdateAsync).join();
        try {
            log.info("jobDetails updated: "+jobDetailsUpdateAsync.get());
            log.info("jobDetailsHistory updated: "+jobDetailsHistoryUpdateAsync.get());
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body("jobDetails | jobDetailsHistory Updated");
    }

    // WF (TABLE) LEVEL Auditing
    public ResponseEntity<String> auditLogRunAsync(Map<String, Object> payload) {

        String Target_Audit_Schema_Nm = (String) payload.get(SSIAppConstants.Target_Audit_Schema_Nm);
        int JOB_NUM = (int) payload.get(SSIAppConstants.JOB_NUM);
        Object BATCH_START_DATE = payload.get(SSIAppConstants.BATCH_START_DATE);
        Object BATCH_END_DATE = payload.get(SSIAppConstants.BATCH_END_DATE);
        int BATCH_ID = (int) payload.get(SSIAppConstants.BATCH_ID);
        int SUB_BATCH_ID = (int) payload.get(SSIAppConstants.SUB_BATCH_ID);
        int STATUS_NUM = (int) payload.get(SSIAppConstants.STATUS_NUM);
        String TABLE_NAME = (String) payload.get(SSIAppConstants.TABLE_NAME);
        int WF_NUM = (int) payload.get(SSIAppConstants.WF_NUM);
        String REMARKS = (String) payload.get(SSIAppConstants.REMARKS);
        int STG_CNT = (int) payload.get(SSIAppConstants.STG_CNT);
        int MIS_CNT = (int) payload.get(SSIAppConstants.MIS_CNT);
        int matchFlag = STG_CNT == MIS_CNT ? 1 : 0;

        List<Object> BUSINESS_START_DATE = auditLogService.findBusinessStartDate(Target_Audit_Schema_Nm, WF_NUM);
        Timestamp BUSINESS_START_DATE_TS = (Timestamp) BUSINESS_START_DATE.get(0);

        Object BUSINESS_END_DATE = payload.get(SSIAppConstants.BUSINESS_END_DATE);
        Timestamp BUSINESS_END_DATE_TS = Timestamp.valueOf(BUSINESS_END_DATE.toString());

        AuditLog auditLog = new AuditLog(JOB_NUM, BATCH_ID, SUB_BATCH_ID, WF_NUM, TABLE_NAME, Timestamp.valueOf(BATCH_START_DATE.toString()), Timestamp.valueOf(BATCH_END_DATE.toString()), BUSINESS_START_DATE_TS, BUSINESS_END_DATE_TS, STATUS_NUM, REMARKS, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), null, null, null, null);
        log.trace("auditLogObj: " + auditLog);
        ControlTable controlTable = new ControlTable(JOB_NUM, BATCH_ID, SUB_BATCH_ID, WF_NUM, TABLE_NAME, STG_CNT, MIS_CNT, matchFlag);
        log.trace("controlTableObj: " + controlTable);

        // persist object in db
        CompletableFuture<AuditLog> auditLogSavedAsync = auditLogService.saveOrUpdateAsync(auditLog);
        CompletableFuture<ControlTable> controlTableSavedAsync = controlTableService.persistAsync(controlTable);

        CompletableFuture.allOf(auditLogSavedAsync, controlTableSavedAsync).join();
        try {
            log.info("auditLog saved: "+auditLogSavedAsync.get());
            log.info("controlTable saved: "+controlTableSavedAsync.get());
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body("auditLog | controlTable saved");
    }

    // WF (TABLE) LEVEL Auditing Exception
    public ResponseEntity<String> auditLogExceRunAsync(Map<String, Object> payload) {

        String Target_Audit_Schema_Nm = (String) payload.get(SSIAppConstants.Target_Audit_Schema_Nm);
        int JOB_NUM = (int) payload.get(SSIAppConstants.JOB_NUM);
        Object BATCH_START_DATE = payload.get(SSIAppConstants.BATCH_START_DATE);
        Object BATCH_END_DATE = payload.get(SSIAppConstants.BATCH_END_DATE);
        int BATCH_ID = (int) payload.get(SSIAppConstants.BATCH_ID);
        int SUB_BATCH_ID = (int) payload.get(SSIAppConstants.SUB_BATCH_ID);
        int STATUS_NUM = (int) payload.get(SSIAppConstants.STATUS_NUM);
        String TABLE_NAME = (String) payload.get(SSIAppConstants.TABLE_NAME);
        int WF_NUM = (int) payload.get(SSIAppConstants.WF_NUM);
        String REMARKS = (String) payload.get(SSIAppConstants.REMARKS);

        List<Object> BUSINESS_START_DATE = auditLogService.findBusinessStartDate(Target_Audit_Schema_Nm, WF_NUM);
        Timestamp BUSINESS_START_DATE_TS = (Timestamp) BUSINESS_START_DATE.get(0);

        Object BUSINESS_END_DATE = payload.get(SSIAppConstants.BUSINESS_END_DATE);
        Timestamp BUSINESS_END_DATE_TS = Timestamp.valueOf(BUSINESS_END_DATE.toString());

        String Failure_Point = (String) payload.get(SSIAppConstants.Failure_Point);
        Integer Technical_Error_Code = (Integer) payload.get(SSIAppConstants.Technical_Error_Code);
        String Technical_Error_Desc = (String) payload.get(SSIAppConstants.Technical_Error_Desc);
        Integer REPROCESS_RETRY = (Integer) payload.get(SSIAppConstants.REPROCESS_RETRY);


        AuditLog auditLog = new AuditLog(JOB_NUM, BATCH_ID, SUB_BATCH_ID, WF_NUM, TABLE_NAME, Timestamp.valueOf(BATCH_START_DATE.toString()), Timestamp.valueOf(BATCH_END_DATE.toString()), BUSINESS_START_DATE_TS, BUSINESS_END_DATE_TS, STATUS_NUM, REMARKS, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Failure_Point, Technical_Error_Code, Technical_Error_Desc, REPROCESS_RETRY);
        log.trace("auditLogObj: " + auditLog);

        // persist object in db
        CompletableFuture<AuditLog> auditLogSavedAsync = auditLogService.saveOrUpdateAsync(auditLog);
        CompletableFuture<Integer> jobDetailsUpdateAsync = jobDetailsService.updateByBatchIdSubBatchIdAsync(JOB_NUM, BATCH_ID, SUB_BATCH_ID, null, STATUS_NUM, null, Failure_Point, Technical_Error_Code, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Technical_Error_Desc);
        CompletableFuture<Integer> jobDetailsHistoryUpdateAsync = jobDetailsHistoryService.updateByBatchIdSubBatchIdAsync(JOB_NUM, BATCH_ID, SUB_BATCH_ID, null, STATUS_NUM, null, Failure_Point, Technical_Error_Code, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Technical_Error_Desc);

        CompletableFuture.allOf(auditLogSavedAsync, jobDetailsUpdateAsync,jobDetailsHistoryUpdateAsync).join();
        try {
            log.info("auditLogExec saved: "+auditLogSavedAsync.get());
            log.info("jobDetails updated: "+jobDetailsUpdateAsync.get());
            log.info("jobDetailsHistory updated: "+jobDetailsHistoryUpdateAsync.get());
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body("auditLog | jobDetails | jobDetailsHistory Updated");
    }

    // JOB LEVEL Auditing Exception
    public ResponseEntity<String> jobLevelExceRunAsync(Map<String, Object> payload)  {

        String Target_Audit_Schema_Nm = (String) payload.get(SSIAppConstants.Target_Audit_Schema_Nm);
        int JOB_NUM = (int) payload.get(SSIAppConstants.JOB_NUM);
        int BATCH_ID = (int) payload.get(SSIAppConstants.BATCH_ID);
        int SUB_BATCH_ID = (int) payload.get(SSIAppConstants.SUB_BATCH_ID);
        String Failure_Point = (String) payload.get(SSIAppConstants.Failure_Point);
        Integer Technical_Error_Code = (Integer) payload.get(SSIAppConstants.Technical_Error_Code);
        String Technical_Error_Desc = (String) payload.get(SSIAppConstants.Technical_Error_Desc);
        int STATUS_NUM = (int) payload.get(SSIAppConstants.STATUS_NUM);

        // persist object in db
        CompletableFuture<Integer> jobDetailsUpdateAsync = jobDetailsService.updateByBatchIdSubBatchIdAsync(JOB_NUM, BATCH_ID, SUB_BATCH_ID, null, STATUS_NUM, null, Failure_Point, Technical_Error_Code, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Technical_Error_Desc);
        CompletableFuture<Integer> jobDetailsHistoryUpdateAsync = jobDetailsHistoryService.updateByBatchIdSubBatchIdAsync(JOB_NUM, BATCH_ID, SUB_BATCH_ID, null, STATUS_NUM, null, Failure_Point, Technical_Error_Code, RealTimeAudit.getStatusDesc(STATUS_NUM).replace("'", ""), Technical_Error_Desc);

        CompletableFuture.allOf(jobDetailsUpdateAsync,jobDetailsHistoryUpdateAsync).join();
        try {
            log.info("jobDetails updated: "+jobDetailsUpdateAsync.get());
            log.info("jobDetailsHistory updated: "+jobDetailsHistoryUpdateAsync.get());
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body("jobDetails | jobDetailsHistory Updated");
    }
}
