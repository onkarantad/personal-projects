package com.sapiens.ssi.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Target {
	
	String target_database;
	String target_database_uri;
	String target_database_name;
	String target_audit_database_name;
	int incr_loop_ind;
	int batch_enable_ind;
	int batch_size;
	String target_zone_id;
	
}
