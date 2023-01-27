package com.sapiens.redis.service.impl.dedup;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.config.cache.MultiCache;
import com.sapiens.redis.service.common.MethodGetterService;
import com.sapiens.redis.service.dedup.DaoDedupService;
import com.sapiens.redis.service.dedup.DedupQueryService;
import com.sapiens.redis.threadhandler.CustomFutureCached;
import com.sapiens.redis.threadhandler.CustomFutureUnCached;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DaoDedupe implements DaoDedupService {

	@Autowired
	private MultiCache multiCache;

	@Autowired
	DedupQueryService dedupQueryService;

	@Autowired
	private NamedParameterJdbcTemplate dbSession;

	@Autowired
	private MethodGetterService methodGetterService;

	@Autowired
	CustomFutureUnCached customFutureUnCached;

	@Autowired
	CustomFutureCached customFutureCached;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Application application;


	private HashOperations hashOperations;
	private RedisTemplate redisTemplate;

	static int cnt = 0;

	@Autowired
	private RedissonClient redisson;

	public static RAtomicLong MAX_CEID;

	public DaoDedupe(RedisTemplate redisTemplate) {
		this.hashOperations = redisTemplate.opsForHash();
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Integer getNrtCeId(ObjectNode entity) {
		Long processStart = System.currentTimeMillis();
		String rowKey = getDedupKey(entity); // lowerCase rKey
		if (multiCache.getDedupCache().getName() != null && rowKey != null) {
			RAtomicLong maxCeId = redisson.getAtomicLong(SSIRedisAppConstants.myAtomicLong);

			if (hashOperations.get(multiCache.getDedupCache().getName(), rowKey) == null) {
				maxCeId.incrementAndGet();
				hashOperations.putIfAbsent(multiCache.getDedupCache().getName(), rowKey, maxCeId.toString());
			}
			int ceId = Integer.valueOf(hashOperations.get(multiCache.getDedupCache().getName(), rowKey).toString());

			log.info(rowKey + " | ceIdHash : " + ceId + " | in " + (System.currentTimeMillis() - processStart) + " ms");
			return ceId;
		} else {
			log.error("Dedup cache not created - ");
			return null;
		}
	}

	@Override
	public List<Object> getMicroJobCeId(Map<String, Object> payload) {
		log.trace("<--> ----------------------------------------------- <-->");
		log.trace("get CE_ID process started");
		String microjobQuery = multiCache.getDedupCache().getSql();
		String microjobQueryFilter = multiCache.getDedupCache().getMicroBatch();
		microjobQuery = microjobQuery + " WHERE " + microjobQueryFilter;

		log.debug("batch CMS query: "+microjobQuery);

		AtomicInteger loadInd = new AtomicInteger();
		loadInd.set(1);

		if (payload.containsKey(SSIRedisAppConstants.loadInd))
			loadInd.set((int) payload.get(SSIRedisAppConstants.loadInd));

		String start = payload.get(SSIRedisAppConstants.start).toString();
		String end = payload.get(SSIRedisAppConstants.end).toString();

		String dedupQuery = null;
		log.trace("get MbDedupe_Query(if/else) from cache started");
		if (payload.containsKey(SSIRedisAppConstants.refresh))
			if ((int) payload.get(SSIRedisAppConstants.refresh) == 1) {
				hashOperations.delete(SSIRedisAppConstants.DedupConfig,
						SSIRedisAppConstants.mbDedupeQuery + loadInd.get());
			}
		if (hashOperations.hasKey(SSIRedisAppConstants.DedupConfig,
				SSIRedisAppConstants.mbDedupeQuery + loadInd.get())) {
			dedupQuery = hashOperations
					.get(SSIRedisAppConstants.DedupConfig, SSIRedisAppConstants.mbDedupeQuery + loadInd.get())
					.toString();
			dedupQuery = dedupQuery.replace(SSIRedisAppConstants.businessStartDate, start)
					.replace(SSIRedisAppConstants.businessEndDate, end);
			log.debug("query_if : " + dedupQuery);
		} else {
			hashOperations.put(SSIRedisAppConstants.DedupConfig, SSIRedisAppConstants.mbDedupeQuery + loadInd,
					dedupQueryService.dedupMBQueryGetter(microjobQuery, loadInd.get()));
			dedupQuery = hashOperations
					.get(SSIRedisAppConstants.DedupConfig, SSIRedisAppConstants.mbDedupeQuery + loadInd.get())
					.toString();
			dedupQuery = dedupQuery.replace(SSIRedisAppConstants.businessStartDate, start)
					.replace(SSIRedisAppConstants.businessEndDate, end);
			log.debug("query_else : " + dedupQuery);
		}
		log.debug("get MbDedupe_Query(if/else) from cache ended");
		MAX_CEID = redisson.getAtomicLong("myAtomicLong");
		log.debug("MAX_CEID_defined: " + MAX_CEID);

		if (loadInd.get() == 0) {
			MAX_CEID.set(0);
			redisTemplate.delete(multiCache.getDedupCache().getName());
		}

		List<Object> mergedFiltered = callLoadDedupMT(dedupQuery);
		return mergedFiltered;

	}

	private List<Object> callLoadDedupMT(String dedupQuery) {

		// get resultSet
		List<Map<String, Object>> resultMapList = Collections
				.synchronizedList(methodGetterService.DBUtilFindAll(dedupQuery));

		log.debug("resultMapList size: " + resultMapList.size());
		log.trace("resultMapList: " + resultMapList.stream().limit(5).collect(Collectors.toList()));

		List<Map<String, Object>> resultMapListWithNulls = resultMapList.parallelStream()
				.filter(v -> (v.get(SSIRedisAppConstants.dedupAttr) == null)).collect(Collectors.toList());
		log.debug("resultMapListWithNulls size: " + resultMapListWithNulls.size());
		log.trace("resultMapListWithNulls: " + resultMapListWithNulls.stream().limit(5).collect(Collectors.toList()));

		if (resultMapListWithNulls.size() > 0) {
			CompletableFuture.runAsync(() -> {
				auditInsert(resultMapListWithNulls);
			});
		}

		// remove null's from resultset
		List<Map<String, Object>> resultMapListWithoutNulls = resultMapList.parallelStream()
				.filter(v -> !(v.get(SSIRedisAppConstants.dedupAttr) == null)).collect(Collectors.toList());

		log.debug("resultMapListWithOutNulls: " + resultMapListWithoutNulls.size());
		log.trace("resultMapListWithOutNulls: "
				+ resultMapListWithoutNulls.stream().limit(5).collect(Collectors.toList()));

		// get dedup attributes
		List<String> resultUniqueKeyList = ((List<String>) (List<?>) resultMapListWithoutNulls.stream()
				.filter(u -> u.containsKey(SSIRedisAppConstants.dedupAttr))
				.map(u -> u.get(SSIRedisAppConstants.dedupAttr)).distinct().filter(i -> i != null)
				.collect(Collectors.toList())).stream().map(String::toLowerCase).collect(Collectors.toList());

		log.debug("resultMapListWithoutNullsUnique DEDUP_ATTR: " + resultUniqueKeyList.size());
		log.trace("resultMapListWithoutNullsUnique DEDUP_ATTR: "
				+ resultUniqueKeyList.stream().limit(5).collect(Collectors.toList()));

		// get all cache values
		Map<Object, String> redisCacheMap = hashOperations.entries(multiCache.getDedupCache().getName());
		log.debug("redisCacheMap size: " + redisCacheMap.size());
		log.trace("redisCacheMap: "
				+ redisCacheMap.entrySet().stream().limit(5).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

		// get cache value which are present in resultSet
		Map<Object, String> cachedMap = resultUniqueKeyList.stream().filter(redisCacheMap::containsKey)
				.collect(Collectors.toMap(Function.identity(), redisCacheMap::get));

		log.debug("cachedMap: " + cachedMap.size());
		log.trace("cachedMap: "
				+ cachedMap.entrySet().stream().limit(5).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

		// convert cachedMap to CachedMapList
		List<Map<String, Object>> cachedMapList = new LinkedList<Map<String, Object>>();

		cachedMap.entrySet().stream().forEach(i -> {
			cachedMapList.add(new HashMap<String, Object>() {
				{
					String k = String.valueOf(i.getKey());
					String v = String.valueOf(i.getValue());
					// log.info("key:value -> "+k+":"+v);
					put(SSIRedisAppConstants.dedupAttr, k);
					put(SSIRedisAppConstants.ceId, v);
				}
			});
		});

		log.debug("cachedMapList: " + cachedMapList.size());
		log.trace("cachedMapList: " + cachedMapList.stream().limit(5).collect(Collectors.toList()));

		Future<List<Map<String, Object>>> futureCallCached = null;
		if (!cachedMapList.isEmpty()) {
			futureCallCached = customFutureCached.futureCallCached(cachedMapList, resultMapListWithoutNulls);
		}

		List<String> unCachedKeys = resultUniqueKeyList.stream().filter(i -> !redisCacheMap.containsKey(i))
				.collect(Collectors.toList());
		log.debug("unCachedKeys: " + unCachedKeys.size());
		log.trace("unCachedKeys: " + unCachedKeys.stream().limit(5).collect(Collectors.toList()));

		List<Map<String, Object>> unCachedMapListMerged = Collections
				.synchronizedList(new LinkedList<Map<String, Object>>());
		if (!unCachedKeys.isEmpty()) {
			unCachedMapListMerged
					.addAll(customFutureUnCached.callFuture(unCachedKeys, cachedMapList, resultMapListWithoutNulls));
			customFutureUnCached.closeExecutor();
		}
		log.debug("unCachedMapListMerged: " + unCachedMapListMerged.size());
		log.trace("unCachedMapListMerged: " + unCachedMapListMerged.stream().limit(5).collect(Collectors.toList()));

		List<Map<String, Object>> merged = Collections.synchronizedList(new LinkedList<Map<String, Object>>());
		if (!cachedMapList.isEmpty()) {
			merged = customFutureCached.getFutureMML(futureCallCached);
			customFutureCached.closeExecutor();
		}

		if (!unCachedMapListMerged.isEmpty()) {
			merged.addAll(unCachedMapListMerged);
			log.info("ummatched_merged added matched");
		}

		log.debug("merged after ummatched_merged added: " + merged.size());
		log.trace("merged after ummatched_merged added: " + merged.stream().limit(5).collect(Collectors.toList()));

		List<Object> mergedFinal = merged
				.stream().map(m -> m.entrySet().stream()
						.filter(map -> !(map.getKey().equals(SSIRedisAppConstants.dedupAttr))).collect(Collectors.toMap(
								x -> x.getKey(), x -> x.getValue())))/* .filter(n->n.containsKey(Constants.CE_ID)) */
				.collect(Collectors.toList());

		log.info("response size: " + mergedFinal.size());
		log.trace("mergedFinal: " + mergedFinal.stream().limit(5).collect(Collectors.toList()));

		log.trace("return json of CE_ID ended");
		log.trace("<--> ----------------------------------------------- <-->");

		return mergedFinal;
	}

	public void auditInsert(List<Map<String, Object>> resultMapListWithNulls) {

		try (Connection con = dataSource.getConnection()) {
			String redisLogTable = multiCache.getDedupCache().getLogTable();
			log.fatal("some dedup Attributes are null | inserting " + SSIRedisAppConstants.entityId + " into "
					+ redisLogTable);
			Timestamp insertDate = new Timestamp(System.currentTimeMillis());
			StringBuilder logQuery = new StringBuilder();
			logQuery.append("insert into ").append(redisLogTable).append(" values(").append("?, ?").append(")");
			PreparedStatement preparedStatement = con.prepareStatement(logQuery.toString());
			resultMapListWithNulls.stream().forEach(i -> {
				try {
					con.setAutoCommit(false);
					preparedStatement.setTimestamp(1, insertDate);
					preparedStatement.setString(2, (String) i.get(SSIRedisAppConstants.entityId));
					preparedStatement.addBatch();
				} catch (SQLException e) {
					log.error("prepared statement failed - " + e);
				}
			});

			int[] insertCount = preparedStatement.executeBatch();
			log.debug("audit log inserts: " + insertCount.length);
			con.commit();
			con.setAutoCommit(true);
		} catch (Exception e) {
			log.error("redis log insersion failed - " + e);
		}

	}

	public String getDedupKey(ObjectNode entity) {
		List<String> cacheKeyList = new LinkedList<String>();
		entity.fields().forEachRemaining(i -> cacheKeyList.add(i.getValue().asText()));
		String dKey = null;
		dKey = String.join("|", cacheKeyList);
		dKey = methodGetterService.dateToString(dKey);
		return dKey.toLowerCase();
	}
}
