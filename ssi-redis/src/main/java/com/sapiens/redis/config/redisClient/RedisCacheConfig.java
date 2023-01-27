package com.sapiens.redis.config.redisClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sapiens.redis.config.application.Application;
import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ResourceUtils;

@Log4j2
@Configuration
@ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "redis", matchIfMissing = true)
public class RedisCacheConfig {


    /** The redisson. */
    private RedissonClient redisson;

    /** The active profile. */
    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    Application application;

    /**
     * Redisson client.
     *
     * @return the redisson client
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient()  {
        String configFileName = "redis-" + activeProfile + ".yml";
        File resourceURL = null;
        try {
            resourceURL = ResourceUtils.getFile(application.getCache().getConfigPath() + configFileName);
            Config config = Config.fromYAML(resourceURL);
            redisson = Redisson.create(config);
        } catch (FileNotFoundException e) {
            log.error("file not found in given path ("+application.getCache().getConfigPath()+") "+e);
        } catch (IOException e) {
            log.error("incompatible file "+e);
        }

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
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        String configFileName = "cache-config-" + activeProfile + ".yml";
        return new RedissonSpringCacheManager(redissonClient, "classpath:/" + configFileName);
    }

}