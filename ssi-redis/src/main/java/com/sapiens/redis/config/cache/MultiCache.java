package com.sapiens.redis.config.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "multicache.caches")
@Getter
@Setter
@ToString
public class MultiCache {
	private LookupParam lookupCache ;
	private DedupParam dedupCache ;
}
