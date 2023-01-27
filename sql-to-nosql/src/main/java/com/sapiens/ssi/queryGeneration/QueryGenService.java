package com.sapiens.ssi.queryGeneration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QueryGenService {

    public Optional<Object> getFullLoadStoredQuery(List<Map<String, Object>> DB_CONFIG_MAP_LIST );
    public Optional<Object> getIncrLoadStoredQuery(List<Map<String, Object>> DB_CONFIG_MAP_LIST );
}
