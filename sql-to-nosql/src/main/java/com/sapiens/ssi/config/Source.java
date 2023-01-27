package com.sapiens.ssi.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Source {
	
	String source_database;
	String source_driver_class;
	String source_jdbc_url;
	String source_user_id;
	String source_password;
	String source_schema_nm;
	String audit_schema_nm;
	String source_zone_id;
	
}
