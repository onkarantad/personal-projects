package com.sapiens.ssiAudit.service.dbService;

public interface DBService {

    // returns BATCH_ID
    String mjBatchIDqueryJPA();

    // returns SUB_BATCH_ID
    String mjSubBatchIDqueryJPA();


    // returns Previous Batch Start Date
    String mjPreviousBatchStartDateQueryJPA();


    // returns Previous Batch End Date
    String mjPreviousBatchEndDateQueryJPA();


    // gives combined result
    default String mjCombineJobDetailsparamsQueryJPA() {
        String batchIDquery = mjBatchIDqueryJPA();
        String subBatchIDquery = mjSubBatchIDqueryJPA();
        String previousBatchStartDateQuery = mjPreviousBatchStartDateQueryJPA();
        String previousBatchEndDateQuery = mjPreviousBatchEndDateQueryJPA();

        return "SELECT * FROM (" + batchIDquery + ") a " +
                "CROSS JOIN (" + subBatchIDquery + ") b " +
                "CROSS JOIN (" + previousBatchStartDateQuery + ") c " +
                "CROSS JOIN (" + previousBatchEndDateQuery + ") d ";
    }


    // Update Job Details
    default String mjUpdateJobDetailsQueryJPA() {
        return "update {h-schema}JOB_DETAILS " +
                "set CORE_DB_BATCH_END_DATE= :CORE_DB_BATCH_END_DATE  , " +
                "STATUS_NUM =  :STATUS_NUM , CURRENT_BATCH_END_DATE= :CURRENT_BATCH_END_DATE ,  " +
                "Failure_Point= :Failure_Point , Technical_Error_Code = :Technical_Error_Code , " +
                "Status_Desc= :Status_Desc,	Technical_Error_Desc= :Technical_Error_Desc " +
                "where JOB_NUM= :JOB_NUM  AND BATCH_ID= :BATCH_ID  AND " +
                "SUB_BATCH_ID= :SUB_BATCH_ID";
    }


    // Update Job Details History
    default String mjUpdateJobDetailsHistoryQueryJPA() {
        return "update {h-schema}JOB_DETAILS_History " +
                "set CORE_DB_BATCH_END_DATE= :CORE_DB_BATCH_END_DATE , " +
                "STATUS_NUM = :STATUS_NUM ,  CURRENT_BATCH_END_DATE= :CURRENT_BATCH_END_DATE ," +
                "Failure_Point= :Failure_Point, Technical_Error_Code= :Technical_Error_Code , " +
                "Status_Desc= :Status_Desc,	Technical_Error_Desc= :Technical_Error_Desc " +
                "where JOB_NUM= :JOB_NUM  AND BATCH_ID=  :BATCH_ID  AND " +
                "SUB_BATCH_ID= :SUB_BATCH_ID";
    }

    // returns Business Start Date
    String loadJobsBusinessStartDateQueryJPA();


}
