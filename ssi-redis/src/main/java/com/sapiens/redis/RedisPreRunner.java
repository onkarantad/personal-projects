package com.sapiens.redis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.threadhandler.CustomFutureLookup;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sapiens.redis.config.cache.MultiCache;
//import com.sapiens.redis.config.ReadMultiCacheConfig;
import com.sapiens.redis.service.common.MethodGetterService;
import com.sapiens.redis.service.dedup.DedupQueryService;
import com.sapiens.redis.threadhandler.CustomFutureDedup;
//import com.sapiens.redis.threadhandler.CustomFutureLookup;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RedisPreRunner implements CommandLineRunner {

    @Autowired
    private MultiCache multiCache;

    @Autowired
    CustomFutureLookup customFutureLookup;

    // Get Redis Client Connection
    @Autowired
    private RedissonClient redisson;

    private HashOperations hashOperations;
    private RedisTemplate redisTemplate;

    @Autowired
    private DedupQueryService dedupService;

    @Autowired
    CustomFutureDedup customFutureDedup;

     @Autowired
     private Application application;

    @Autowired
    private MethodGetterService methodGetterService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    /*
     * cache for lookup maps (map1,map2,...) for multiple lookup map RMapCache will
     * assign ==> key=table_name value=RMapCache
     */
    public static Map<String, RMapCache<String, String>> lookupCacheMaps = new HashMap<>();

    /* if more than 1 value present ==> key=table_name value= list_of_lokup_value */
    public static Map<String, List<String>> lookupFieldValueMap = new HashMap<>();

    public RedisPreRunner(RedisTemplate redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {

        log.debug("command Line Runner Started");

        // Redis Dedupe pre load init
        if (multiCache.getDedupCache().getEnableInd()) {
            dedupCacheCreator();
        }

        if (multiCache.getLookupCache().getEnableInd()) {
            lookupCacheCreator();
        }

        log.debug("command Line Runner Ended");

    }

    public void dedupCacheCreator() {
        log.info("DEDUP PreLoad cache started");
        long start = System.currentTimeMillis();

        StringBuilder queryCMS = new StringBuilder();
        String queryFilter = null;
        if (!multiCache.getDedupCache().getName().isEmpty()) {
            queryCMS.append(multiCache.getDedupCache().getSql());
            queryFilter = multiCache.getDedupCache().getPreLoad();
            queryCMS.append(" WHERE ").append(queryFilter);
        }

        if (queryCMS != null && multiCache.getDedupCache().getName() != null) {
            // redis cache object for dedup cache
            log.info("dedupName: " + multiCache.getDedupCache().getName());
            // RMapCache<String, Integer> redisMap = redisson.getMapCache(dedupName);

            String dedupQuery = null;
            try {
                dedupQuery = dedupService.dedupPreLoadQueryGetter(queryCMS.toString());
            } catch (SQLException e) {
                log.error("Dedup Query Generation failed - " + e);
            } catch (Exception e) {
                log.error("Dedup Query Generation failed - " + e);
                e.printStackTrace();
            }

            log.info("dedup_query: " + dedupQuery);

            callPreLoadDedupMT(dedupQuery, start);

            log.info("DEDUP PreLoad cache done");
        }
    }

    /*
     * This method is used to cache pre load dedupe attribute with ce_id into Redis
     * concurrently
     */
    private void callPreLoadDedupMT(String dedupQuery, long start) {

        // get distinct keys from redis cache
        // sample - [1975-01-01 00:00:00|whnvmk.xpowpl@gmail.com|9yscscuzox, 1975-01-01
        // 00:00:00|cgcwnh.wfbvrh@gmail.com|9wbovgtxkl]
        Set<String> cachedKeySet = hashOperations.keys(multiCache.getDedupCache().getName());


        log.debug("size of cache data which is already loaded: " + cachedKeySet.size());
        log.trace("cachedKeySet: " + cachedKeySet.stream().limit(5).collect(Collectors.toList()));

        // get resultset from dedupQuery
        // sample - {|eee@eee.com|8528528=1, |leagal@selenium.com|3333333=140036,
        // |orit1@gmail.com|1234=1
        Map<String, String> resultMap = methodGetterService.findAll(jdbcTemplate, dedupQuery);

        log.debug("resultMap size: " + resultMap.size());
        log.trace("resultMap: "
                + resultMap.entrySet().stream().limit(5).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

        // get dedup attributes from resultSet
        // sample - [|leagal@selenium.com|3333333, |orit1@gmail.com|1234,
        // |eee@eee.com|8528528]
        List<String> dedupAttrKeyList = resultMap.keySet().parallelStream().distinct().collect(Collectors.toList());
        log.debug("dedupAttrKeyList size: " + dedupAttrKeyList.size());
        log.trace("dedupAttrKeyList: " + dedupAttrKeyList.stream().limit(5).collect(Collectors.toList()));

        // get keys which are not present in cache memory
        // sample - [|ron@idit.co.il|, ||, |fdfds@fdf.ff|, |kfire@sapiens.com|,
        // |gracej@hotmail.com|]
        List<String> unCachedKeyList = new ArrayList<>((CollectionUtils.removeAll(dedupAttrKeyList, cachedKeySet)));
        log.debug("unCachedKeyList size: " + unCachedKeyList.size());
        log.trace("unCachedKeyList: " + unCachedKeyList.stream().limit(5).collect(Collectors.toList()));

        // get map from resultMap which is not present in cache
        // sample - {1988-06-26 00:00:00||=350600, |fdfds@fdf.ff|=1,
        // |gracej@hotmail.com|=83986, |ron@idit.co.il|=1, ||=349459}
        Map<Object, String> unCachedMap = unCachedKeyList.parallelStream().filter(resultMap::containsKey)
                .collect(Collectors.toMap(Function.identity(), resultMap::get));

        log.debug("unCachedMap size: " + unCachedMap.size());
        log.trace("unCachedMap: "
                + unCachedMap.entrySet().stream().limit(5).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

//        List<Map<String, Object>> unCachedMapList = new LinkedList<Map<String, Object>>();
//
//        unCachedMap.entrySet().stream().forEach(i -> {
//            unCachedMapList.add(new HashMap<String, Object>() {
//                {
//                    String k = String.valueOf(i.getKey());
//                    String v = String.valueOf(i.getValue());
//                    log.debug("unCached key:value -> " + k + ":" + v);
//                    put(SSIRedisAppConstants.dedupAttr, k);
//                    put(SSIRedisAppConstants.ceId, v);
//                }
//            });
//        });
//
//        // sample - [{DEDUP_ATTR=||, CE_ID=349459}, {DEDUP_ATTR=|gracej@hotmail.com|,
//        // CE_ID=83986}]
//        log.debug("unCachedMapList size: " + unCachedMapList.size());
//        log.trace("unCachedMapList: " + unCachedMapList.stream().limit(5).collect(Collectors.toList()));

        log.info("Loading dedup PreLoad cache ..");

        // call concurrent method and close executor once done
        if (unCachedMap.size() > 0) {
            customFutureDedup.callFuture(unCachedMap);
            customFutureDedup.closeExecutor();
        }

        long end = System.currentTimeMillis();
        log.info(" Dedup PreLoad cache successfully loaded into Redis, time taken: " + (end - start) + " ms");

    }

    // lookup preload master data into redis DB
    public void lookupCacheCreator() {
        long start = System.currentTimeMillis();
        // process lookup preload master data into redis DB only if it is configured
        if (multiCache.getLookupCache().getLookupCacheList().size() > 0) {

            List<String> lookupKeyList = multiCache.getLookupCache().getLookupCacheList().stream()
                    .map(i -> i.getKey().toUpperCase()).collect(Collectors.toList());
            List<String> lookupValueList = multiCache.getLookupCache().getLookupCacheList().stream()
                    .map(i -> i.getValue().toUpperCase()).collect(Collectors.toList());
            List<String> lookupMapNameList = multiCache.getLookupCache().getLookupCacheList().stream()
                    .map(i -> i.getName().toUpperCase()).collect(Collectors.toList());
            List<Boolean> lookupClearMapList = multiCache.getLookupCache().getLookupCacheList().stream()
                    .map(i -> i.getClearMap()).collect(Collectors.toList());
            List<String> fromClauseList = multiCache.getLookupCache().getLookupCacheList().stream()
                    .map(i -> i.getFromClause().toUpperCase()).collect(Collectors.toList());

            // map for multiple lookup
            for (String lookupMap : lookupMapNameList) {
                RMapCache<String, String> redisMapLookup = redisson.getMapCache(lookupMap);
                lookupCacheMaps.put(lookupMap, redisMapLookup);
            }
            log.debug("lookupMaps : " + lookupCacheMaps);

            List<List<Future>> noWaitLookupExecList = new ArrayList<>();

            Map<String, String> resultMap = null;

            for (int i = 0; i < lookupKeyList.size(); i++) {
                RMapCache<String, String> lookupCacheMap = lookupCacheMaps.get(lookupMapNameList.get(i));

                Boolean clearMap = lookupClearMapList.get(i);
                if (clearMap) {
                    log.info(i + " truncate lookup map (" + lookupMapNameList.get(i) + "): " + lookupCacheMap.delete());
                }

                List<String> lookupKey = Arrays.asList(lookupKeyList.get(i).split(","));
                List<String> lookupValue = Arrays.asList(lookupValueList.get(i).split(","));
                String fromClause = fromClauseList.get(i);
                String mapName = lookupMapNameList.get(i);

                String lookupQuery = null;

                lookupFieldValueMap.put(mapName, lookupValue);

                String sqk = lookupKey.size() > 1
                        ? String.join(",'" + application.getKeyConcatChar() + "',", lookupKey)
                        : lookupKey.toString().replace("[", "").replace("]", "");
                String sqv = lookupValue.size() > 1
                        ? String.join(",'" + application.getKeyConcatChar() + "',", lookupValue)
                        : lookupValue.toString().replace("[", "").replace("]", "");
                if (lookupKey.size() == 1 && lookupValue.size() > 1) {
                    lookupQuery = "select UPPER(" + sqk + "),concat(" + sqv + ")" + "from " + fromClause;
                } else if (lookupKey.size() > 1 && lookupValue.size() == 1) {
                    lookupQuery = "select UPPER(concat(" + sqk + ")),(" + sqv + ")" + "from " + fromClause;
                } else if (lookupKey.size() > 1 && lookupValue.size() > 1) {
                    lookupQuery = "select  UPPER(concat(" + sqk + ")),concat(" + sqv + ")" + "from " + fromClause;
                } else if (lookupKey.size() == 1 && lookupValue.size() == 1) {
                    lookupQuery = "select UPPER(" + sqk + "),(" + sqv + ")" + "from " + fromClause;
                }

                log.info(i + " lookup_query (" + lookupMapNameList.get(i) + ") ==> " + lookupQuery);

                log.info(i + " creating lookup cache (" + lookupMapNameList.get(i) + ") ... ");

                resultMap = methodGetterService.findAll(jdbcTemplate, lookupQuery);

                log.debug("resultMap size: " + resultMap.size());

                if (resultMap.size() > 0) {

//                    resultMap.entrySet().stream().forEach(j->{
//                        String key = j.getKey().toUpperCase();
//					String value = j.getValue();
//					lookupCacheMap.putIfAbsent(key, value);
//                    });

                    noWaitLookupExecList.add(customFutureLookup.callFuture(resultMap, lookupCacheMap));
                }
                long end = System.currentTimeMillis();
                log.info(i + " lookup cache (" + lookupMapNameList.get(i) + ") loaded successfully, time taken:  "
                        + (end - start) + " ms");
            }

            if (resultMap.size() > 0) {
                noWaitLookupExecList.stream().forEach(t -> {
                    customFutureLookup.isLookupExecuting(t);
                });
                customFutureLookup.closeExecutor();
            }

            log.info("Lookup PreLoad cache done");
        }

    }

}