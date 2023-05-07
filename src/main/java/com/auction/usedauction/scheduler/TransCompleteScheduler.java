package com.auction.usedauction.scheduler;

import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile(value = {"local", "production"})
public class TransCompleteScheduler {

    private final AuctionService auctionService;
    private final AuctionHistoryService auctionHistoryService;

    @Async
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    // 경매 Trans 상태 확인 및 변경 후 ban 처리
    public void checkTransCompleteAndBan() {
        log.info("경매 Trans 상태 확인 및 변경 후 ban 처리 scheduler 실행");
        // 낙찰 성공된 경매 중 경매 종료 후 일주일이 지났는데도 아직 거래 확정이 안된 것들 Trans 상태 변환
        LocalDateTime criteriaTime = LocalDateTime.now().minusDays(7);
        List<Long> changedAuctionIds = auctionService.changeBulkAuctionMemberTransAndAuctionStatus(criteriaTime);
        log.info("경매 Trans 상태 확인 및 변경 완료");

        // ban 처리
        for (Long changedAuctionId : changedAuctionIds) {
            auctionHistoryService.banMemberByAuctionId(changedAuctionId);
        }
        log.info("밴 완료");

    }
}
