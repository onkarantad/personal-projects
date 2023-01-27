package com.sapiens.redis.threadhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.config.cache.MultiCache;
import com.sapiens.redis.service.impl.dedup.DaoDedupe;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CustomFutureUnCached {

	private static ExecutorService pool ;

	@Autowired
	DaoDedupe microBatchDedupServiceImpl;

	@Autowired
	MultiCache multiCache;

	@Autowired
	Application application;

	private static HashOperations hashOperations;
	private RedisTemplate redisTemplate;

	public CustomFutureUnCached(RedisTemplate redisTemplate) {
		this.hashOperations = redisTemplate.opsForHash();
		this.redisTemplate = redisTemplate;
	}

	public List<Map<String, Object>> callFuture(List<String> workLoad, List<Map<String, Object>> matchedMapList,
												List<Map<String, Object>> resultMapList) {

		// int thrdSize = 50;
		log.warn("BatchThrdSize -> " + application.getServices().getMicrobatch().getThread());
		pool = Executors.newFixedThreadPool(application.getServices().getMicrobatch().getThread());

		int workLoadSize = workLoad.size();
		log.warn("workLoadSize -> " + workLoadSize);

		int batch = 0;
		if (workLoadSize <= 10)
			batch = 1;
		else if (workLoadSize > 10 && workLoadSize <= 50)
			batch = 5;
		else if (workLoadSize > 50 && workLoadSize <= 100)
			batch = 10;
		else if (workLoadSize > 100 && workLoadSize <= 1000)
			batch = 15;
		else if (workLoadSize > 1000 && workLoadSize <= 3000)
			batch = 20;
		else if (workLoadSize > 3000 && workLoadSize <= 5000)
			batch = 30;
		else if (workLoadSize > 5000 && workLoadSize <= 8000)
			batch = 50;
		else if (workLoadSize > 8000 && workLoadSize <= 15000)
			batch = 100;
		else
			batch = application.getServices().getMicrobatch().getBatch();

		log.warn("batch -> " + batch);
		List<List<String>> partitions = ListUtils.partition(workLoad, batch);

		List<CompletableFuture<List<Map<String, Object>>>> results = new LinkedList<>();
		for (List<String> list : partitions) {

			CompletableFuture<List<Map<String, Object>>> mergedMapList = noWaitExec(list, pool ,resultMapList);
			results.add(mergedMapList);
		}

		log.warn("Future_Executing_Uncached_Map...");
		List<Map<String, Object>> merged = new ArrayList<Map<String, Object>>();

		for (CompletableFuture<List<Map<String, Object>>> completableFuture : results) {
			// try {
			while (!completableFuture.isDone()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			try {
				merged.addAll((List<Map<String, Object>>) completableFuture.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		}
		log.debug("merged: " + merged);
		return merged;
	}

	public CompletableFuture<List<Map<String, Object>>> noWaitExec(final List<String> batch, ExecutorService pool,
																   List<Map<String, Object>> resultMapList) {

		return CompletableFuture.supplyAsync(() -> {
			List<Map<String, Object>> unMatchedMapList = new LinkedList<>();

			batch.stream().forEach(attr -> {
				long maxCeId = microBatchDedupServiceImpl.MAX_CEID.incrementAndGet();
				hashOperations.putIfAbsent(multiCache.getDedupCache().getName(), attr, maxCeId);

				unMatchedMapList.add(new HashMap<String, Object>() {
					{
						// log.warn("key:value -> "+attr+":"+maxCeId);
						put(SSIRedisAppConstants.dedupAttr, attr);
						put(SSIRedisAppConstants.ceId, maxCeId);
					}
				});
			});
			return unMatchedMapList;
		}, pool).thenCompose(unMatchedSubMapList -> CompletableFuture.supplyAsync(() -> {
			List<Map<String, Object>> mergedInn = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> map1 : unMatchedSubMapList) {
				boolean found = false;
				for (Map<String, Object> map2 : resultMapList) {
					if (map1.get(SSIRedisAppConstants.dedupAttr) != null && map2.get(SSIRedisAppConstants.dedupAttr) != null) {
						if (map1.get(SSIRedisAppConstants.dedupAttr).toString().equalsIgnoreCase(map2.get(SSIRedisAppConstants.dedupAttr).toString())) {
							found = true;
							Map<String, Object> copy = new HashMap<>();
							copy.putAll(map1);
							copy.putAll(map2);
							mergedInn.add(copy);
						}
					}
				}
				if (!found) {
					mergedInn.add(map1);
				}
			}
			return mergedInn;
		}));

	}

	public static void closeExecutor() {
		pool.shutdown();
	}
}
