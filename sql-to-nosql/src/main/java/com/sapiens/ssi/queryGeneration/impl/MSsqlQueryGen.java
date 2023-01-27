package com.sapiens.ssi.queryGeneration.impl;

import com.sapiens.ssi.constants.SSIConstant;
import com.sapiens.ssi.exceptions.ConfigMappingException;
import com.sapiens.ssi.queryGeneration.QueryGenService;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class MSsqlQueryGen implements QueryGenService {


    @Override
    public Optional<Object> getFullLoadStoredQuery(List<Map<String, Object>> DB_CONFIG_MAP_LIST) {
        if (DB_CONFIG_MAP_LIST == null || DB_CONFIG_MAP_LIST.size() == 0){
            throw new ConfigMappingException("<" + MSsqlQueryGen.class.getSimpleName() + "> DB_CONFIG_MAP_LIST is null");
        }
        log.debug("DB_CONFIG_MAP_LIST: " + DB_CONFIG_MAP_LIST);
       Optional<Object> storedQuery = Optional.ofNullable(DB_CONFIG_MAP_LIST.stream().filter(f ->
               f.get(SSIConstant.Transformation_Expression).toString().equalsIgnoreCase(SSIConstant.STORED_QUERY_FULL_LOAD)
       ).collect(Collectors.toList()).get(0).get(SSIConstant.Source_Table));


        return storedQuery;
    }

    @Override
    public Optional<Object> getIncrLoadStoredQuery(List<Map<String, Object>> DB_CONFIG_MAP_LIST) {
        if (DB_CONFIG_MAP_LIST == null || DB_CONFIG_MAP_LIST.size() == 0){
            throw new ConfigMappingException("<" + MSsqlQueryGen.class.getSimpleName() + "> DB_CONFIG_MAP_LIST is null");
        }
        log.debug("DB_CONFIG_MAP_LIST: " + DB_CONFIG_MAP_LIST);
        Optional<Object> storedQuery = Optional.ofNullable(DB_CONFIG_MAP_LIST.stream().filter(f ->
                f.get(SSIConstant.Transformation_Expression).toString().equalsIgnoreCase(SSIConstant.STORED_QUERY_INCR_LOAD)
        ).collect(Collectors.toList()).get(0).get(SSIConstant.Source_Table));


        return storedQuery;
    }
}
