package com.sapiens.ssi.auditing;

public class AuditPayload {
    public static String getPreScriptPayload(String Target_Audit_Schema_Nm,int JOB_NUM, String Source_Zone_Id,String CURRENT_BATCH_START_DATE){
        return "{\n" +
                "    \"Target_Audit_Schema_Nm\": \""+Target_Audit_Schema_Nm+"\",\n" +
                "    \"JOB_NUM\": "+JOB_NUM+",\n" +
                "    \"STATUS_NUM\":1,\n" +
                "    \"Source_Zone_Id\": \""+Source_Zone_Id+"\",\n" +
                "    \"CURRENT_BATCH_START_DATE\": \""+CURRENT_BATCH_START_DATE+"\"\n" +
                "}";
    }

    public static String getPostScriptPayload(String Target_Audit_Schema_Nm,int JOB_NUM,String Source_Zone_Id,String CURRENT_BATCH_END_DATE,int BATCH_ID,int SUB_BATCH_ID){
        return "{\n" +
                "    \"Target_Audit_Schema_Nm\": \""+Target_Audit_Schema_Nm+"\",\n" +
                "    \"JOB_NUM\": "+JOB_NUM+",\n" +
                "    \"STATUS_NUM\":3,\n" +
                "    \"Source_Zone_Id\": \""+Source_Zone_Id+"\",\n" +
                "    \"CURRENT_BATCH_END_DATE\": \""+CURRENT_BATCH_END_DATE+"\",\n" +
                "    \"BATCH_ID\": "+BATCH_ID+",\n" +
                "    \"SUB_BATCH_ID\": "+SUB_BATCH_ID+"\n" +
                "}";
    }

    public static String getAuditLogPayload(String Target_Audit_Schema_Nm, int JOB_NUM,String Source_Zone_Id,String BATCH_START_DATE,String BATCH_END_DATE,int BATCH_ID,int SUB_BATCH_ID,String TABLE_NAME,int WF_NUM,String REMARKS /*,String BUSINESS_END_DATE*/,int STG_CNT,int MIS_CNT){
        return "{\n" +
                "    \"Target_Audit_Schema_Nm\": \""+Target_Audit_Schema_Nm+"\",\n" +
                "    \"JOB_NUM\": "+JOB_NUM+",\n" +
                "    \"STATUS_NUM\":3,\n" +
                "    \"Source_Zone_Id\": \""+Source_Zone_Id+"\",\n" +
                "    \"BATCH_START_DATE\": \""+BATCH_START_DATE+"\",\n" +
                "    \"BATCH_END_DATE\": \""+BATCH_END_DATE+"\",\n" +
                "    \"BATCH_ID\":"+BATCH_ID+",\n" +
                "    \"SUB_BATCH_ID\":"+SUB_BATCH_ID+",\n" +
                "    \"TABLE_NAME\":\""+TABLE_NAME+"\",\n" +
                "    \"WF_NUM\":"+WF_NUM+",\n" +
                "    \"REMARKS\":\""+REMARKS+"\",\n" +
                "    \"BUSINESS_END_DATE\":\"2022-10-10 15:10:00\",\n" +
                "    \"STG_CNT\":"+STG_CNT+",\n" +
                "    \"MIS_CNT\":"+MIS_CNT+"\n" +
                "    }";
    }

    public static String getAuditLogExcePayload(String Target_Audit_Schema_Nm, int JOB_NUM, String Source_Zone_Id,String BATCH_START_DATE,String BATCH_END_DATE,int BATCH_ID,int SUB_BATCH_ID,String TABLE_NAME,int WF_NUM,String REMARKS,String Failure_Point,String Technical_Error_Desc){
        return "{\n" +
                "    \"Target_Audit_Schema_Nm\": \""+Target_Audit_Schema_Nm+"\",\n" +
                "    \"JOB_NUM\": "+JOB_NUM+",\n" +
                "    \"STATUS_NUM\":2,\n" +
                "    \"Source_Zone_Id\": \""+Source_Zone_Id+"\",\n" +
                "    \"BATCH_START_DATE\": \""+BATCH_START_DATE+"\",\n" +
                "    \"BATCH_END_DATE\": \""+BATCH_END_DATE+"\",\n" +
                "    \"BATCH_ID\":"+BATCH_ID+",\n" +
                "    \"SUB_BATCH_ID\":"+SUB_BATCH_ID+",\n" +
                "    \"TABLE_NAME\":\""+TABLE_NAME+"\",\n" +
                "    \"WF_NUM\":"+WF_NUM+",\n" +
                "    \"REMARKS\":\""+REMARKS+"\",\n" +
                "    \"BUSINESS_END_DATE\":\"2022-10-10 15:10:00\",\n" +
                "    \"Failure_Point\":\""+Failure_Point+"\",\n" +
                "    \"Technical_Error_Code\":1,\n" +
                "    \"Technical_Error_Desc\":\""+Technical_Error_Desc+"\"" +
                "    }";
    }

    public static String getJobLevelExcePayload(String Target_Audit_Schema_Nm, int JOB_NUM,int BATCH_ID,int SUB_BATCH_ID,String Failure_Point,String Technical_Error_Desc){
        return "{\n" +
                "    \"Target_Audit_Schema_Nm\": \""+Target_Audit_Schema_Nm+"\",\n" +
                "    \"JOB_NUM\": "+JOB_NUM+",\n" +
                "    \"STATUS_NUM\": 2,\n" +
                "    \"BATCH_ID\": "+BATCH_ID+",\n" +
                "    \"SUB_BATCH_ID\": "+SUB_BATCH_ID+",\n" +
                "    \"Failure_Point\": \""+Failure_Point+"\",\n" +
                "    \"Technical_Error_Code\": 1,\n" +
                "    \"Technical_Error_Desc\": \""+Technical_Error_Desc+"\"\n" +
                "}";
    }

}
