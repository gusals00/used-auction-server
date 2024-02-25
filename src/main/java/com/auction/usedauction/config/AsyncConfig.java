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
        executor.setCorePoolSize(60); //한번에 실행 가능한 Thread 수
        executor.setMaxPoolSize(100); //최대 Thread pool 크기
        executor.setQueueCapacity(70); // CorePoolSize넘어서는 요청이 오면 queue에 쌓을 수 있는 최대 요청 수
        executor.setThreadNamePrefix("THREAD-ASYNC-"); // 생성되는 Thread 접두사
        executor.initialize();
        return executor;
    }
}
