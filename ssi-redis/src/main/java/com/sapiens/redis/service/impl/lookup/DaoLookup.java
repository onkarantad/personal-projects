package com.sapiens.redis.service.impl.lookup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sapiens.redis.RedisPreRunner;
import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.service.common.MethodGetterService;
import com.sapiens.redis.service.lookup.DaoLookupService;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

@Log4j2
@Service
public class DaoLookup implements DaoLookupService {
    @Autowired
    RedisPreRunner redisPreRunner;

    @Autowired
    MethodGetterService methodGetterService;

    @Autowired
    Application application;

    @Override
    public String getLookupKey(ObjectNode entity) {
        StringJoiner lKey = new StringJoiner(application.getKeyConcatChar());
        entity.get(SSIRedisAppConstants.lookupKey).fields().forEachRemaining(
                i -> lKey.add(i.getValue().toString().replaceAll("\"", "").replace("[", "").replace("]", "")));
        return lKey.toString().toUpperCase();
    }

    @Override
    public String getLookupValue(ObjectNode entity) {
        String mapName = entity.get(SSIRedisAppConstants.mapName).asText().toUpperCase();
        if (redisPreRunner.lookupCacheMaps.containsKey(mapName)) {
            RMapCache<String, String> redisMapLookup = redisPreRunner.lookupCacheMaps.get(mapName);
            String lKey = getLookupKey(entity);
            List<String> lookupValueList = RedisPreRunner.lookupFieldValueMap.get(mapName);
            boolean isArrayPresent = false;
            Iterator<Entry<String, JsonNode>> fields = entity.get(SSIRedisAppConstants.lookupKey).fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> i = fields.next();
                if (i.getValue().isArray()) {
                    isArrayPresent = true;
                }
            }
            if (isArrayPresent) {
                return methodGetterService.getValueByFilter(entity, redisMapLookup, lookupValueList);
            } else if (redisMapLookup.containsKey(lKey)) {
                if (entity.has(SSIRedisAppConstants.lookupValue)) {
                    String lookupValue = entity.get(SSIRedisAppConstants.lookupValue).asText().toUpperCase();
                    if (lookupValueList.contains(lookupValue)) {
                        int i = lookupValueList.indexOf(lookupValue);
                        log.info("response: " + redisMapLookup.get(lKey).split(application.getKeyConcatChar())[i]);
                        return redisMapLookup.get(lKey).split(application.getKeyConcatChar())[i];
                    } else {
                        log.error("LOOKUP_VALUE not available - ");
                        return null;
                    }
                } else {
                    log.info("response: " + redisMapLookup.get(lKey));
                    return redisMapLookup.get(lKey);
                }
            } else {
                log.error("Lookup cache not available for provided key - ");
                return null;
            }

        } else {
            log.error("Lookup cache not created - ");
            return null;
        }
    }

}
