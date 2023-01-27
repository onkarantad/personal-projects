package com.sapiens.ssi.service;

public interface DBService {

    default String mstJobInputQuery(String Target_Audit_Schema_Nm) {
        return "SELECT " + Target_Audit_Schema_Nm + ".MST_JOB.JOB_NUM," + Target_Audit_Schema_Nm + ".MST_JOB.JOB_NAME,"
                + Target_Audit_Schema_Nm + ".MST_JOB.STATUS_NUM," + Target_Audit_Schema_Nm
                + ".MST_JOB.INCR_LOAD_IND FROM " + Target_Audit_Schema_Nm + ".MST_JOB where " + Target_Audit_Schema_Nm
                + ".MST_JOB.STATUS_NUM=4 and "+Target_Audit_Schema_Nm+".MST_JOB.JOB_NAME='SSI_NOSQL' order by " + Target_Audit_Schema_Nm + ".MST_JOB.JOB_NUM";
    }

    default String mstWFInputQuery(String Target_Audit_Schema_Nm, int JOB_NUM) {
        return "SELECT " + Target_Audit_Schema_Nm + ".MST_WORKFLOW.JOB_NUM," + Target_Audit_Schema_Nm
                + ".MST_WORKFLOW.WF_NUM," + Target_Audit_Schema_Nm + ".MST_WORKFLOW.WF_NAME," + Target_Audit_Schema_Nm
                + ".MST_WORKFLOW.STATUS_NUM," + Target_Audit_Schema_Nm + ".MST_WORKFLOW.PARENT_WF_NUM,"
                + Target_Audit_Schema_Nm + ".MST_WORKFLOW.SOURCE_WF_NUM," + Target_Audit_Schema_Nm
                + ".MST_WORKFLOW.DATE_IND," + Target_Audit_Schema_Nm + ".MST_WORKFLOW.INCR_INDENTIFIER_COLS,"
                + Target_Audit_Schema_Nm + ".MST_WORKFLOW.Sequence,"
                + Target_Audit_Schema_Nm + ".MST_WORKFLOW.MAPPING_TYPE FROM " + Target_Audit_Schema_Nm
                + ".MST_WORKFLOW where " + Target_Audit_Schema_Nm + ".MST_WORKFLOW.STATUS_NUM=4" + " and "
                + Target_Audit_Schema_Nm + ".MST_WORKFLOW.JOB_NUM = " + JOB_NUM;
    }
    // 5 and WF_NUM IN (4)

    default String cjmsInputQuery(String Target_Audit_Schema_Nm) {
        return "SELECT " + Target_Audit_Schema_Nm + ".SSI_DWH_CJMS.Join_Tables," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CJMS.src_table_1," + Target_Audit_Schema_Nm + ".SSI_DWH_CJMS.src_table_2,"
                + Target_Audit_Schema_Nm + ".SSI_DWH_CJMS.src_table_2_Alias," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CJMS.on_clause," + Target_Audit_Schema_Nm + ".SSI_DWH_CJMS.where_clause FROM "
                + Target_Audit_Schema_Nm + ".SSI_DWH_CJMS";
    }

    default String cmsInputQuery(String Target_Audit_Schema_Nm, String WF_NAME) {
        return "SELECT " + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Target_Table_Name," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CMS.Target_Column_Name," + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Target_Data_Type,"
                + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Source_Table," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CMS.select_clause_column," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CMS.Source_Column_Data_Type," + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Scd_Type,"
                + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.ODS_Tables," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CMS.Join_Condition," + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.SUB_JOIN_CONDITION,"
                + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.combine," + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Group_by,"
                + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Transformation_Expression," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CMS.Target_Table_PK FROM " + Target_Audit_Schema_Nm + ".SSI_DWH_CMS where "
                + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.Target_Table_Name ='" + WF_NAME + "' ORDER BY "
                + Target_Audit_Schema_Nm + ".SSI_DWH_CMS.combine," + Target_Audit_Schema_Nm
                + ".SSI_DWH_CMS.Target_Column_Name";
    }



}
