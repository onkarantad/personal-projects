package com.sapiens.redis.config.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ToString
public class DedupParam {
	String name;
	String sql;
	String preLoad;
	String microBatch;
	String logTable;
	Boolean enableInd;
}
