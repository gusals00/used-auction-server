package com.auction.usedauction.scheduler;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.dto.SellerAndBuyerIdDTO;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.AuctionService;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
    private final AuctionRepository auctionRepository;
    private final AuctionHistoryQueryRepository auctionHistoryQueryRepository;
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
        // 특정경매가 거래 실패일 경우
        for (Long changedAuctionId : changedAuctionIds) {
            boolean isTransFail = auctionRepository.existsAuctionByIdAndStatus(changedAuctionId, AuctionStatus.TRANSACTION_FAIL);
            if (isTransFail) {
                SellerAndBuyerIdDTO sellerAndBuyerId = auctionHistoryQueryRepository.findSellerAndBuyerId(changedAuctionId)
                        .orElseThrow(() -> new CustomException(AuctionErrorCode.INVALID_AUCTION));
                // 판매자 구매자를 밴 해야 하는지 확인
                banUser(sellerAndBuyerId.getSellerId(), MemberType.Seller);
                banUser(sellerAndBuyerId.getBuyerId(), MemberType.Buyer);
            }
        }
        log.info("밴 완료");

    }

    private void banUser(Long memberId, MemberType memberType) {
        try {
            auctionHistoryService.banMember(memberId);
        } catch (CustomException e) {
            e.printStackTrace();
            // 판매자가 존재하지 않습니다 판매자 ID = {}
            // 구매자가 존재하지 않습니다 구매자 ID = {}
            log.error(memberType.name + "가 존재하지 않습니다. " + memberType.name + "= {}", memberId);
        }
    }

    @Getter
    @AllArgsConstructor
    enum MemberType {
        Buyer("판매자"),
        Seller("구매자");

        private final String name;
    }
}
