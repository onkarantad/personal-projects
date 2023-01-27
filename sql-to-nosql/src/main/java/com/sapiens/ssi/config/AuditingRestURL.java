package com.sapiens.ssi.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuditingRestURL {
    String pre_script_url;
    String post_script_url;
    String augit_log_url;
    String audit_log_exce_url;
    String job_level_exce_url;
}
