package com.sapiens.redis.config.redisClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import com.sapiens.redis.config.application.Application;
import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.sapiens.redis.config.redisClient.ApplicationProperties.CacheProperties;

@Log4j2
@Configuration
@ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "jcache")
public class RedisJCacheConfig {

	/** The redisson. */
	private RedissonClient redisson;

	/** The active profile. */
	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Autowired
	Application application;

	/** The application properties. */
	@Autowired
	private ApplicationProperties applicationProperties;
	/**
	 * Redisson client.
	 *
	 * @return the redisson client
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		String configFileName = "redis-" + activeProfile + ".yml";
		System.out.println("con : "+configFileName);

		File resourceURL = null;
		Config config=null;
		try {
			resourceURL = ResourceUtils.getFile(application.getCache().getConfigPath() + configFileName);
			config = Config.fromYAML(resourceURL);
		} catch (FileNotFoundException e) {
			log.error("file not found in given path ("+application.getCache().getConfigPath()+") "+e);
		} catch (IOException e) {
			log.error("incompatible file "+e);
		}

		redisson = Redisson.create(config);
		return redisson;
	}


	/**
	 * Jedis connection factory.
	 *
	 * @param redissonClient the redisson client
	 * @return the redis connection factory
	 */
	@Bean
	public RedisConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
		return new RedissonConnectionFactory(redissonClient);
	}

	/**
	 * Redis template.
	 *
	 * @param redissonConnectionFactory the redisson connection factory
	 * @return the redis template
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redissonConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redissonConnectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		return template;
	}

	/**
	 * Redisson configuration.
	 *
	 * @param <K>            the key type
	 * @param <V>            the value type
	 * @param redissonClient the redisson client
	 * @return the javax.cache.configuration. configuration
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public <K, V> javax.cache.configuration.Configuration<?, ?> redissonConfiguration(RedissonClient redissonClient) {
		return RedissonConfiguration.fromInstance(redissonClient);
	}
	/*
	 * The Class CachingSetup.
	 **/
	@Component
	@DependsOn("redissonClient")
	public class CachingSetup implements JCacheManagerCustomizer {

		/**
		 * Customize cache manager. Adding ttl for each cache
		 *
		 * @param cacheManager the cache manager
		 */
		@Override
		public void customize(CacheManager cacheManager) {
			List<CacheProperties> cacheConfigs = applicationProperties.getCache();
			for (CacheProperties cacheConfig : cacheConfigs) {
				MutableConfiguration<Object, Object> config = new MutableConfiguration<Object, Object>();
				long ttl = cacheConfig.getTtl().getSeconds();
				if (ttl > 0) {
					config.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, ttl)));
				}
				javax.cache.configuration.Configuration<?, ?> jconfig = RedissonConfiguration.fromInstance(redisson,
						config);
				cacheManager.createCache(cacheConfig.getCacheName(), jconfig);
			}
		}
	}

}