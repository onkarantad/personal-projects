package com.sapiens.ssiAudit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CONTROL_TABLE")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ControlTable {
    @Id
    Integer JOB_ID;
    Integer BATCH_ID;
    Integer SUB_BATCH_ID;
    Integer WF_NUM;
    String TABLE_NAME;
    Integer SOURCE_CNT;
    Integer STG_CNT;
    Integer MIS_CNT;
    Integer DM_CNT;
    Integer MATCH_FLAG;
    Double AGG_SRC;
    Double AGG_TRGT;
    Double AGG_SRC1;
    Double AGG_TRGT1;
    String STATUS_DESC;
    String Failure_Point;
    Integer Technical_Error_Code;
    String Technical_Error_Desc;

    public ControlTable(Integer JOB_ID, Integer BATCH_ID, Integer SUB_BATCH_ID, Integer WF_NUM, String TABLE_NAME, Integer STG_CNT, Integer MIS_CNT, Integer MATCH_FLAG) {
        this.JOB_ID = JOB_ID;
        this.BATCH_ID = BATCH_ID;
        this.SUB_BATCH_ID = SUB_BATCH_ID;
        this.WF_NUM = WF_NUM;
        this.TABLE_NAME = TABLE_NAME;
        this.STG_CNT = STG_CNT;
        this.MIS_CNT = MIS_CNT;
        this.MATCH_FLAG = MATCH_FLAG;
    }
}
