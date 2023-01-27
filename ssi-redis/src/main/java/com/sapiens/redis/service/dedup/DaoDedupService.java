package com.sapiens.redis.service.dedup;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public interface DaoDedupService {

	List<Object> getMicroJobCeId(Map<String, Object> payload) throws SQLException;

	Integer getNrtCeId(ObjectNode entity);
	
}
