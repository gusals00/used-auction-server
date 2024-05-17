package com.auction.usedauction;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Profile(value = {"local","production"})
public class InitDB {
    private final InitDBService initDBService;
    private final InitProcedure initProcedure;
    @PostConstruct
    public void init() throws IOException {
        //initDBService.init();
        //initDBService.initScheduler();
        // 더미 데이터 추가 프로시저 실행 함수
//        initProcedure.initProcedure();
    }
}
