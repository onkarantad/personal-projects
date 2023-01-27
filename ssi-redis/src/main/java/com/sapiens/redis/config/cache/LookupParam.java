package com.sapiens.redis.config.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@ToString
public class LookupParam {
	Boolean enableInd;
	List<LookupCacheList> lookupCacheList;
}
