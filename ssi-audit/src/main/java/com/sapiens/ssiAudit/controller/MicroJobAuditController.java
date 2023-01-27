package com.sapiens.ssiAudit.controller;

import com.sapiens.ssiAudit.controller.runner.MicroJobAuditControllerRunner;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Log4j2
@RestController
@RequestMapping(path = "/audit/microJob")
public class MicroJobAuditController {

    @Autowired
    MicroJobAuditControllerRunner microJobAuditControllerRunner;

    /*
    preScript payload:
    {
    "Target_Audit_Schema_Nm": "NOSQL_AUDIT",
    "JOB_NUM": 1,
    "STATUS_NUM":1,
    "Source_Zone_Id": "Asia/Jerusalem",
    "CURRENT_BATCH_START_DATE": "2023-01-13 15:19:00"
    }
     */
    // JOB LEVEL pre scripting
    @PostMapping("/preScript")
    public List<Map<String, Object>> preScriptAsync(@RequestBody Map<String, Object> payload) throws ExecutionException, InterruptedException {
        log.debug("preScriptPayload: " + payload);
        return microJobAuditControllerRunner.preScriptRunAsync(payload);
    }

    /*
    postScript payload:
    {
    "Target_Audit_Schema_Nm": "NOSQL_AUDIT",
    "JOB_NUM": 1,
    "STATUS_NUM":3,
    "Source_Zone_Id": "Asia/Jerusalem",
    "CURRENT_BATCH_END_DATE": "2023-01-13 16:19:00",
    "BATCH_ID": 1,
    "SUB_BATCH_ID": 1
    }
     */
    // JOB LEVEL post scripting
    @PostMapping("/postScript")
    public ResponseEntity<String> postScriptAsync(@RequestBody Map<String, Object> payload) {
        log.debug("postScriptPayload: " + payload);
        return microJobAuditControllerRunner.postScriptRunAsync(payload);
    }

    /*
    auditLog payload:
    {
    "Target_Audit_Schema_Nm": "NOSQL_AUDIT",
    "JOB_NUM": 1,
    "STATUS_NUM":3,
    "Source_Zone_Id": "Asia/Jerusalem",
    "BATCH_START_DATE": "2023-01-10 18:04:00",
    "BATCH_END_DATE": "2023-01-10 18:10:00",
    "BATCH_ID":1,
    "SUB_BATCH_ID":1,
    "TABLE_NAME":"CONTACT",
    "WF_NUM":1,
    "REMARKS":"stored_query",
    "BUSINESS_END_DATE":"2022-10-10 15:10:00",
    "STG_CNT":1,
    "MIS_CNT":1
    }
     */
    // WF (TABLE) LEVEL Auditing
    @PostMapping("/auditLog")
    public ResponseEntity<String> auditLogAsync(@RequestBody Map<String, Object> payload) {
        log.debug("auditLogPayload: " + payload);
        return microJobAuditControllerRunner.auditLogRunAsync(payload);
    }

    /*
    auditLogException payload:
    {
    "Target_Audit_Schema_Nm": "NOSQL_AUDIT",
    "JOB_NUM": 1,
    "STATUS_NUM": 2,
    "Source_Zone_Id": "Asia/Jerusalem",
    "BATCH_START_DATE": "2022-11-22 18:04:00",
    "BATCH_END_DATE": "2022-11-22 18:10:00",
    "BATCH_ID": 1,
    "SUB_BATCH_ID": 1,
    "TABLE_NAME": "CONTACT",
    "WF_NUM": 1,
    "REMARKS": "stored_query",
    "BUSINESS_END_DATE": "2022-10-10 15:10:00",
    "Failure_Point": "tJavaFLEX22",
    "Technical_Error_Code": 1,
    "Technical_Error_Desc": "NULL PT EXEC22",
    "REPROCESS_RETRY": 0
    }
     */
    // WF (TABLE) LEVEL Auditing Exception
    @PostMapping("/auditLogExce")
    public ResponseEntity<String> auditLogExceAsync(@RequestBody Map<String, Object> payload) {
        log.debug("auditLogExecPayload: " + payload);
        return microJobAuditControllerRunner.auditLogExceRunAsync(payload);
    }

    /*
    jobLevelException payload:
    {
    "Target_Audit_Schema_Nm": "NOSQL_AUDIT",
    "JOB_NUM": 1,
    "STATUS_NUM": 2,
    "BATCH_ID": 1,
    "SUB_BATCH_ID": 1,
    "Failure_Point": "tJavaFLEX22 JobLevel",
    "Technical_Error_Code": 1,
    "Technical_Error_Desc": "NULL PT JobLevel"
    }
     */
    // JOB LEVEL Auditing Exception
    @PostMapping("/jobLevelExce")
    public ResponseEntity<String> jobLevelExceAsync(@RequestBody Map<String, Object> payload) {
        log.debug("jobLevelExcePayload: " + payload);
        return microJobAuditControllerRunner.jobLevelExceRunAsync(payload);
    }

}
