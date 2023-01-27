package com.sapiens.ssiAudit.service.dbService.impl;

import com.sapiens.ssiAudit.service.dbService.DBService;
import com.sapiens.ssiAudit.constants.SSIAppConstants;

public class SQLserverDBImpl implements DBService {

    // returns BATCH_ID
    @Override
    public String mjBatchIDqueryJPA() {
        return "select case WHEN MAX (BATCH_ID) IS NULL THEN 1 \r\n"
                + " WHEN DateDiff(d, CURRENT_BATCH_START_DATE, GetDate()) = 0 THEN MAX (BATCH_ID) \r\n"
                + " WHEN DateDiff(d, CURRENT_BATCH_START_DATE, GetDate()) = -1 THEN MAX (BATCH_ID) \r\n"
                + " WHEN DateDiff(d, CURRENT_BATCH_START_DATE, GetDate()) > 0 THEN MAX (BATCH_ID)+1 END AS BATCH_ID from "
                + "{h-schema}JOB_DETAILS \r\n" + " where JOB_NUM= :JOB_NUM " + "\r\n"
                + "group by CURRENT_BATCH_START_DATE";
    }


    // returns SUB_BATCH_ID
    @Override
    public String mjSubBatchIDqueryJPA() {
        return "SELECT CASE WHEN MAX (SUB_BATCH_ID) IS NULL " +
                "THEN 1 ELSE MAX(SUB_BATCH_ID)+1 END AS SUB_BATCH_ID FROM " +
                "{h-schema}JOB_DETAILS " +
                "WHERE DateDiff(d, CURRENT_BATCH_START_DATE, GetDate()) = 0 " +
                "OR DateDiff(d, CURRENT_BATCH_START_DATE, GetDate()) = -1 AND JOB_NUM= :JOB_NUM ";

    }


    // returns Previous Batch Start Date
    @Override
    public String mjPreviousBatchStartDateQueryJPA() {
        return "select CURRENT_BATCH_START_DATE from ( SELECT CASE WHEN " +
                "CURRENT_BATCH_START_DATE IS NULL THEN "
                + "'" + SSIAppConstants.Default_Date + "'"
                + " ELSE CURRENT_BATCH_START_DATE END AS CURRENT_BATCH_START_DATE " +
                "FROM {h-schema}JOB_DETAILS where job_num = :JOB_NUM ) t";

    }


    // returns Previous Batch End Date
    @Override
    public String mjPreviousBatchEndDateQueryJPA() {
        return "select CURRENT_BATCH_END_DATE " +
                "from ( SELECT CASE WHEN CURRENT_BATCH_END_DATE IS NULL THEN "
                + "'" + SSIAppConstants.Default_Date + "'"
                + " ELSE CURRENT_BATCH_END_DATE END AS CURRENT_BATCH_END_DATE " +
                "FROM {h-schema}JOB_DETAILS where job_num = :JOB_NUM ) t";
    }

    // returns Business Start Date
    @Override
    public String loadJobsBusinessStartDateQueryJPA() {
        return "select " +
                "cast(coalesce(max((business_end_date)),'1800-01-01 00:00:00.0000') as datetime2) " +
                "as BUSINESS_END_DATE from {h-schema}AUDIT_LOG where WF_NUM= :WF_NUM";
    }

}
