package com.sapiens.ssiAudit.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // CORE_POOL_SIZE : initial live threads
    @Value("${spring.async.core-pool-size}")
    private Integer CORE_POOL_SIZE;

    // MAX_POOL_SIZE : max threads in pool
    @Value("${spring.async.max-pool-size}")
    private Integer MAX_POOL_SIZE;

    // QUEUE_CAPACITY : if queue exceeds the capacity required threads fetched from thread pol
    @Value("${spring.async.queue-capacity}")
    private Integer QUEUE_CAPACITY;
    @Value("${spring.async.pool-name}")
    private String POOL_NAME;

    // Enable Async in Application
    @Bean("asyncExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix(POOL_NAME + "-");
        return executor;
    }
}
