package com.auction.usedauction;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile(value = {"test"})
public class InitTest {

    private final InitTestService initTestService;

    @PostConstruct
    public void init() {
//        initTestService.initTest();
    }
}
