package com.sapiens.ssiAudit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="AUDIT_LOG")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer audit_id;
    Integer job_id;
    Integer batch_id;
    Integer sub_batch_id;
    Integer wf_num;
    String table_name;
    Timestamp batch_start_date;
    Timestamp batch_end_date;
    Timestamp business_start_date;
    Timestamp business_end_date;
    Integer status_num;
    String remarks;
    String status_desc;
    String failure_point;
    Integer technical_error_code;
    String technical_error_desc;
    Integer reprocess_retry;

    public AuditLog(Integer job_id, Integer batch_id, Integer sub_batch_id, Integer wf_num, String table_name, Timestamp batch_start_date, Timestamp batch_end_date, Timestamp business_start_date, Timestamp business_end_date, Integer status_num, String remarks, String status_desc, String failure_point, Integer technical_error_code, String technical_error_desc, Integer reprocess_retry) {
        this.job_id = job_id;
        this.batch_id = batch_id;
        this.sub_batch_id = sub_batch_id;
        this.wf_num = wf_num;
        this.table_name = table_name;
        this.batch_start_date = batch_start_date;
        this.batch_end_date = batch_end_date;
        this.business_start_date = business_start_date;
        this.business_end_date = business_end_date;
        this.status_num = status_num;
        this.remarks = remarks;
        this.status_desc = status_desc;
        this.failure_point = failure_point;
        this.technical_error_code = technical_error_code;
        this.technical_error_desc = technical_error_desc;
        this.reprocess_retry = reprocess_retry;
    }
}
