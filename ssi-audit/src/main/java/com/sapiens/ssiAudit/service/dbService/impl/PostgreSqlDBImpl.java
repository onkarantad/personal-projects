package com.sapiens.ssiAudit.service.dbService.impl;

import com.sapiens.ssiAudit.service.dbService.DBService;

public class PostgreSqlDBImpl implements DBService {

    // returns BATCH_ID
    @Override
    public String mjBatchIDqueryJPA() {
        return "select case WHEN MAX (BATCH_ID) IS NULL THEN 1 \r\n"
                + " WHEN date_part('day', LOCALTIMESTAMP - current_batch_start_date\\:\\:timestamp) = 0 THEN MAX (BATCH_ID) \r\n"
                + " WHEN date_part('day', LOCALTIMESTAMP - current_batch_start_date\\:\\:timestamp) = -1 THEN MAX (BATCH_ID) \r\n"
                + " WHEN date_part('day', LOCALTIMESTAMP - current_batch_start_date\\:\\:timestamp) > 0 THEN MAX (BATCH_ID)+1 END from "
                + "{h-schema}JOB_DETAILS \r\n" + " where JOB_NUM= :JOB_NUM \r\n"
                + "group by CURRENT_BATCH_START_DATE";
    }

    // returns SUB_BATCH_ID
    @Override
    public String mjSubBatchIDqueryJPA() {
        return "SELECT CASE WHEN MAX (SUB_BATCH_ID) IS NULL THEN 1 ELSE MAX(SUB_BATCH_ID)+1 END AS SUB_BATCH_ID FROM "
               +"{h-schema}JOB_DETAILS " +
                "WHERE date_part('day', LOCALTIMESTAMP - current_batch_start_date\\:\\:timestamp) = 0 " +
                "OR date_part('day', LOCALTIMESTAMP - current_batch_start_date\\:\\:timestamp) = -1 " +
                "AND JOB_NUM= :JOB_NUM ";
    }

    // returns Previous Batch Start Date
    @Override
    public String mjPreviousBatchStartDateQueryJPA() {
        return "select CURRENT_BATCH_START_DATE from {h-schema}JOB_DETAILS where job_num= :JOB_NUM ";
    }

    // returns Previous Batch End Date
    @Override
    public String mjPreviousBatchEndDateQueryJPA() {
        return "select CURRENT_BATCH_END_DATE from {h-schema}JOB_DETAILS where job_num= :JOB_NUM ";
    }

    // returns Business Start Date
    @Override
    public String loadJobsBusinessStartDateQueryJPA() {
        return "select coalesce(max(business_end_date)\\:\\:timestamp, '1800-01-01 00:00:00.0000'\\:\\:timestamp) from  "
                + "{h-schema}AUDIT_LOG where WF_NUM= :WF_NUM ";
    }
}
