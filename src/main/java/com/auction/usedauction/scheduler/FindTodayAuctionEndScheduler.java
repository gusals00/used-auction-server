package com.auction.usedauction.scheduler;

import com.auction.usedauction.repository.auction_end.AuctionEndRepository;
import com.auction.usedauction.repository.query.AuctionQueryRepository;
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
public class FindTodayAuctionEndScheduler {

    private final AuctionQueryRepository auctionQueryRepository;
    private final AuctionEndRepository auctionEndRepository;

    @Async
    @Scheduled(cron = "20 55 23 * * *")
    // 23:55분일 때마다
    // 다음날 경매 종료인 경매들 저장
    public void putAuctionInfo() {
        log.info("경매 정보 저장 스케쥴러 실행");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withHour(23).withMinute(55).withSecond(0);
        LocalDateTime endDate = startDate.plusDays(1).withMinute(59).withSecond(59);
        auctionEndRepository.clearAll();
        auctionEndRepository.add(auctionQueryRepository.findIdAndEndDateByDate(startDate, endDate));
    }
}
