package com.wuin.wi_mega.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SchedulingConfig {

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(20); // 设置核心线程数
        taskExecutor.setMaxPoolSize(512); // 设置最大线程数
        taskExecutor.setQueueCapacity(1024); // 设置队列容量
        taskExecutor.setThreadNamePrefix("majob-");
        taskExecutor.initialize();
        return taskExecutor;
    }
}