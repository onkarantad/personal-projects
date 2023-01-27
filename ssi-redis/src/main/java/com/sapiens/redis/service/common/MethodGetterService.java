package com.sapiens.redis.service.common;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.marshalling.Pair;
import org.redisson.api.RMapCache;

import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.node.ObjectNode;
/*
 *class MethodGetterService
 *common service for lookup and dedup
 **/
public interface MethodGetterService {

    String getValueByFilter(ObjectNode entity,RMapCache<String, String> redisMapLookup,List<String> lookupValueList);

    LinkedHashMap<String, String> findAll(JdbcTemplate jdbcTemplate,String query);

    List<Map<String, Object>> DBUtilFindAll(String query);

    boolean isDatePresent(String key);

    String dateToString(String key);

    boolean isValidDate(String key);

}
