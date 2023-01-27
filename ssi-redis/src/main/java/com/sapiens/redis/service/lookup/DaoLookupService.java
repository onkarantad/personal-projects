package com.sapiens.redis.service.lookup;

import com.fasterxml.jackson.databind.node.ObjectNode;


public interface DaoLookupService {

	String getLookupKey(ObjectNode entity);

	String getLookupValue(ObjectNode entity);

}
