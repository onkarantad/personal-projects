package com.sapiens.ssi.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Logging {

	String log_folderpath;
	String log_filename;
	String log_filesize;
	String log_rolloversize;
	String log_mode;
	
}
