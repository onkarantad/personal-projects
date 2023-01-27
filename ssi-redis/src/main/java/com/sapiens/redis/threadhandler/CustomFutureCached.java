package com.sapiens.redis.threadhandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.service.impl.dedup.DaoDedupe;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CustomFutureCached {

	public static ExecutorService executor;

	// MML -> matched merged list
	public Future<List<Map<String, Object>>> futureCallCached(List<Map<String, Object>> matchedMapList,
															  List<Map<String, Object>> resultMapList) {

		executor = Executors.newSingleThreadExecutor();

		Future<List<Map<String, Object>>> futureCallCached = (Future<List<Map<String, Object>>>) executor.submit(() -> {
			List<Map<String, Object>> merged = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> map1 : matchedMapList) {
				boolean found = false;
				for (Map<String, Object> map2 : resultMapList) {
					if (map1.get(SSIRedisAppConstants.dedupAttr) != null && map2.get(SSIRedisAppConstants.dedupAttr) != null) {
						if (map1.get(SSIRedisAppConstants.dedupAttr).toString().equalsIgnoreCase(map2.get(SSIRedisAppConstants.dedupAttr).toString())) {
							// if (map1.get(Constants.DEDUP_ATTR).equals(map2.get(Constants.DEDUP_ATTR))) {
							found = true;
							Map<String, Object> copy = new HashMap<>();
							copy.putAll(map1);
							copy.putAll(map2);
							merged.add(copy);
						}
					}
				}
				if (!found) {
					merged.add(map1);
				}
			}
			return merged;
		});

		return futureCallCached;
	}

	public List<Map<String, Object>> getFutureMML(Future<List<Map<String, Object>>> callFuture) {

		List<Map<String, Object>> merged = null ;

		log.warn("Future_Executing_Cached_Map...");
		while (!callFuture.isDone()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			merged = callFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return merged;

	}

	public void closeExecutor() {
		executor.shutdown();
	}

}
