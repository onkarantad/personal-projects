//package com.sapiens.redis.logging;
//
//import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
//import org.springframework.boot.context.logging.LoggingApplicationListener;
//import org.springframework.context.ApplicationEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.annotation.Bean;
//import org.springframework.core.Ordered;
//import org.springframework.core.env.ConfigurableEnvironment;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class LoggingListener implements ApplicationListener, Ordered {
//
//    @Override
//    public int getOrder() {
//        return LoggingApplicationListener.DEFAULT_ORDER - 1;
//    }
//
//    @Override
//    @Bean
//    public void onApplicationEvent(ApplicationEvent event) {
//        if (event instanceof ApplicationEnvironmentPreparedEvent) {
//            ConfigurableEnvironment environment = ((ApplicationEnvironmentPreparedEvent) event).getEnvironment();
//            List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
//            System.out.println("activeProfiles: "+activeProfiles);
//            if (!activeProfiles.contains("dev")) {
//                return;
//            }
//
//            String filepath = environment.getProperty("logging.level.path");
//            // validator.validateProperty(someProp);
//
//            System.setProperty("logging.level.path", filepath);
//        }
//    }
//}