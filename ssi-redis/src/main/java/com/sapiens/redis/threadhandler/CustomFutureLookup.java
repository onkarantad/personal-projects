package com.sapiens.redis.threadhandler;

import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.service.common.MethodGetterService;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
@Log4j2
public class CustomFutureLookup {


	@Autowired
	MethodGetterService methodGetterService;

	@Autowired
	Application application;

	private static ExecutorService pool;

	public List<Future> callFuture(Map<String, String> resultMap, RMapCache<String, String> lookupCacheMap) {

		if (pool == null) {
			log.warn("LookupThrdSize -> " + application.getServices().getLookup().getThread());
			pool = Executors.newFixedThreadPool(application.getServices().getLookup().getThread());
		}

		int workLoadSize = resultMap.size();

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
			batch = application.getServices().getLookup().getBatch();

		int recordsPerChunk=(workLoadSize%batch==0)?(workLoadSize/batch):((workLoadSize/batch)+1);
		AtomicInteger ai = new AtomicInteger();

        Collection<List<Map.Entry<String, String>>> partitions = resultMap.entrySet().parallelStream().collect(Collectors.groupingBy(it->ai.getAndIncrement()/recordsPerChunk)).values();

		List<Future> futureList = new LinkedList<>();

		for (List<Map.Entry<String, String>> list : partitions) {
			Future future = noWaitExec(list,lookupCacheMap, pool);
			futureList.add(future);
		}

		return futureList;

	}

	public Future<Void> noWaitExec(List<Map.Entry<String, String>> batch,RMapCache<String, String> lookupCacheMap,ExecutorService pool) {
		return pool.submit(new Callable<Void>() {
			public Void call() throws Exception {
				batch.stream().forEach(i->{
					String key = i.getKey().toUpperCase();
					String value = i.getValue();
					lookupCacheMap.putIfAbsent(key, value);
				});
				return null;
			}
		});
	}

	public void isLookupExecuting(List<Future> callFuture) {
		log.warn("Future_Executing_Lookup...");
		callFuture.stream().forEach(i->{
			while (!i.isDone()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

	}

	public void closeExecutor() {
		pool.shutdown();
	}

}
