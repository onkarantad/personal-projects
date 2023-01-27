package com.sapiens.ssi.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConfigData {
	
	Source source;
	Target target;
	Logging logging;
	AuditingRestURL auditingRestURL;
}
