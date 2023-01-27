package com.sapiens.redis.config.redisClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.convert.DurationUnit;

public class ApplicationProperties {
	/** The cache. */
	private List<CacheProperties> cache = new ArrayList<ApplicationProperties.CacheProperties>();

	/**
	 * Gets the cache.
	 *
	 * @return the cache
	 */
	public List<CacheProperties> getCache() {
		return cache;
	}
	/**
	 * Sets the cache.
	 *
	 * @param cache the new cache
	 */
	public void setCache(List<CacheProperties> cache) {
		this.cache = cache;
	}

	public static class CacheProperties {

		@NotEmpty
		private String cacheName;

		@DurationUnit(ChronoUnit.SECONDS)
		private Duration ttl = Duration.ofSeconds(-1);
		/** The cache name. */
		public String getCacheName() {
			return cacheName;
		}
		/**
		 * Sets the cache name.
		 *
		 * @param cacheName the new cache name
		 */
		public void setCacheName(String cacheName) {
			this.cacheName = cacheName;
		}
		/**
		 * Gets the ttl.
		 *
		 * @return the ttl
		 */
		public Duration getTtl() {
			return ttl;
		}
		/**
		 * Sets the ttl.
		 *
		 * @param ttl the new ttl
		 */
		public void setTtl(Duration ttl) {
			this.ttl = ttl;
		}

	}
}