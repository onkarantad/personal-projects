package com.sapiens.ssi.queryGeneration.impl;

import com.sapiens.ssi.queryGeneration.QueryGenService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PostgresQueryGen implements QueryGenService {


    @Override
    public Optional<Object> getFullLoadStoredQuery(List<Map<String, Object>> DB_CONFIG_MAP_LIST) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getIncrLoadStoredQuery(List<Map<String, Object>> DB_CONFIG_MAP_LIST) {
        return Optional.empty();
    }
}
