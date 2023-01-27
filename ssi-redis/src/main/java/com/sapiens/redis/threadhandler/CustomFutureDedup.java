package com.sapiens.redis.threadhandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.config.cache.MultiCache;
import com.sapiens.redis.service.impl.dedup.DaoDedupe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
/*
 *The Class CustomFuturePreRunner
 *used to handle threads for pre load
 *
 */
@Service
@Log4j2
public class CustomFutureDedup {

	private static ExecutorService pool ;

	@Autowired
	DaoDedupe microBatchDedupServiceImpl;

	@Autowired
	MultiCache multiCache;

	@Autowired
	Application application;

	private HashOperations hashOperations;
	private RedisTemplate redisTemplate;


	public CustomFutureDedup(RedisTemplate redisTemplate) {
		this.hashOperations = redisTemplate.opsForHash();
		this.redisTemplate = redisTemplate;
	}

	public void callFuture(Map<Object, String> workLoad) {

		log.warn("DedupThrdSize -> " + application.getServices().getDedup().getThread());
		pool = Executors.newFixedThreadPool(application.getServices().getDedup().getThread());

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
			batch = application.getServices().getDedup().getBatch();

		int recordsPerChunk=(workLoadSize%batch==0)?(workLoadSize/batch):((workLoadSize/batch)+1);
		AtomicInteger ai = new AtomicInteger();

		log.warn("batch -> " + batch);
		Collection<List<Map.Entry<Object, String>>> partitions = workLoad.entrySet().parallelStream().collect(Collectors.groupingBy(it->ai.getAndIncrement()/recordsPerChunk)).values();

		log.warn("Future_Executing_Dedup...");

		List<Future> results = new LinkedList<>();

		for (List<Map.Entry<Object, String>> list : partitions) {
			Future future = noWaitExec(list, pool);
			results.add(future);
		}

		for (Future future : results) {
			while (!future.isDone()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Future<Void> noWaitExec(List<Map.Entry<Object, String>> batch, ExecutorService pool) {
		return pool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				batch.stream().forEach(i -> {
					String key = i.getKey().toString().toLowerCase();
					//key = String.join("|", Arrays.asList(key.split("\\|")));
					//log.warn(key);
					String value = i.getValue();
					hashOperations.putIfAbsent(multiCache.getDedupCache().getName(), key, value);

				});
				return null;
			}
		});
	}

	public static void closeExecutor() {
		pool.shutdown();
	}
}
