package com.sapiens.redis.config.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ToString
public class LookupCacheList {
	String name;
	String key;
	String value;
	String fromClause;
	Boolean clearMap;
}
