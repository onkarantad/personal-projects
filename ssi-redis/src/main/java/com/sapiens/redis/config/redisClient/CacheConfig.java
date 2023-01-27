package com.sapiens.redis.config.redisClient;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
/*
 * The Class CacheConfig.
 *
 **/

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport{
	/**
	 * Error handler.
	 *
	 * @return the cache error handler
	 */
	@Override
	public CacheErrorHandler errorHandler() {
		return new AppCacheErrorHandler();
	}
}