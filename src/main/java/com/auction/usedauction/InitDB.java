package com.auction.usedauction;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDB {
    private final InitDBService initDBService;

    @PostConstruct
    public void init() {
        initDBService.initDb();
    }
}
