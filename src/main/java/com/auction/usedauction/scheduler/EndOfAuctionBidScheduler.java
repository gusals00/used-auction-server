package com.auction.usedauction.scheduler;

import com.auction.usedauction.service.AuctionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile(value = {"local","production"})
public class EndOfAuctionBidScheduler {
    private final AuctionHistoryService auctionHistoryService;

    @Async
    @Scheduled(cron = "5 * * * * *")
    // 5초일 때마다
    //경매 종료시 경매 상태를 변경(낙찰 성공/실패로)
    public void changeAuctionStatusToAuctionEndStatuses() {
        log.info("경매 낙찰 스케쥴러 실행");
        auctionHistoryService.changeAuctionStatusToAuctionEndStatuses(LocalDateTime.now());
    }
}
