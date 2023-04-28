package com.auction.usedauction.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); //기본 실행 대기하는 Thread 수
        executor.setMaxPoolSize(100); //동시 동작하는 최대 Thread 수
        executor.setQueueCapacity(30); // 최대 쓰레드 수 보다 많은 요청이 올 경우 queue에 저장 가능한 요청 개수
        executor.setThreadNamePrefix("THREAD-ASYNC-"); // 생성되는 Thread 접두사
        executor.initialize();
        return executor;
    }
}
