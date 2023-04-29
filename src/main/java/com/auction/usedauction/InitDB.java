package com.auction.usedauction;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile(value = {"local","production"})
public class InitDB {
    private final InitDBService initDBService;

    @PostConstruct
    public void init() {
        initDBService.init();
    }
}
