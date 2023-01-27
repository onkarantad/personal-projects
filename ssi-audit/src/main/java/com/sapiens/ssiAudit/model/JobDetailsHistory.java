package com.sapiens.ssiAudit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "JOB_DETAILS_HISTORY")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class JobDetailsHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer job_hist_id;
    Integer job_id;
    Integer batch_id;
    Integer sub_batch_id;
    Integer job_num;
    Timestamp core_db_batch_start_date;
    Timestamp core_db_batch_end_date;
    Timestamp current_batch_start_date;
    Timestamp current_batch_end_date;
    Timestamp previous_batch_start_date;
    Timestamp previous_batch_end_date;
    Integer status_num;
    String failure_point;
    Integer technical_error_code;
    String status_desc;
    String technical_error_desc;

    public JobDetailsHistory(Integer job_id, Integer batch_id, Integer sub_batch_id, Integer job_num, Timestamp core_db_batch_start_date, Timestamp core_db_batch_end_date, Timestamp current_batch_start_date, Timestamp current_batch_end_date, Timestamp previous_batch_start_date, Timestamp previous_batch_end_date, Integer status_num, String status_desc) {
        this.job_id = job_id;
        this.batch_id = batch_id;
        this.sub_batch_id = sub_batch_id;
        this.job_num = job_num;
        this.core_db_batch_start_date = core_db_batch_start_date;
        this.core_db_batch_end_date = core_db_batch_end_date;
        this.current_batch_start_date = current_batch_start_date;
        this.current_batch_end_date = current_batch_end_date;
        this.previous_batch_start_date = previous_batch_start_date;
        this.previous_batch_end_date = previous_batch_end_date;
        this.status_num = status_num;
        this.status_desc = status_desc;
    }
}
