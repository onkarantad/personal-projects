package com.sapiens.redis.config.redisClient;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.lang.Nullable;

import lombok.extern.log4j.Log4j2;


@Log4j2
public class AppCacheErrorHandler extends SimpleCacheErrorHandler {

	/**
	 * Handle cache get error.
	 *
	 * @param exception the exception
	 * @param cache the cache
	 * @param key the key
	 */
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.error("Error in cache get operation ", exception);
	}
	/**
	 * Handle cache put error.
	 *
	 * @param exception the exception
	 * @param cache the cache
	 * @param key the key
	 * @param value the value
	 */
	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
		log.error("Error in cache put operation ", exception);
	}
	/**
	 * Handle cache evict error.
	 *
	 * @param exception the exception
	 * @param cache the cache
	 * @param key the key
	 */
	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.error("Error in cache evict operation ", exception);
	}

	/**
	 * Handle cache clear error.
	 *
	 * @param exception the exception
	 * @param cache the cache
	 */
	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.error("Error in cache clear operation ", exception);
	}
}