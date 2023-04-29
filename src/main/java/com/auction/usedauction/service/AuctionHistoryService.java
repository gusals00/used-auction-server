package com.auction.usedauction.service;

import com.auction.usedauction.aop.RedissonLock;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.AuctionHistoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.auction.usedauction.util.MemberBanConstants.*;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionHistoryService {

    private final AuctionRepository auctionRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final MemberRepository memberRepository;
    @Value("${spring.redis.redisson_bid_lock}")
    private String lockKey;
    private final RedissonClient redissonClient;
//    @Transactional
//    @RedissonLock(keyName = "${lockKey}")
    @Transactional
    public AuctionBidResultDTO biddingAuction(Long auctionId, int bidPrice, String loginId) {

        RLock lock = redissonClient.getLock(lockKey);
        AuctionBidResultDTO bidResult = null;
        try {
            boolean available = lock.tryLock(3, 2, TimeUnit.SECONDS);
            if (!available) { // 입찰 중 락 획득 실패
                CustomException customException = new CustomException(AuctionErrorCode.TRY_AGAIN_BID);
                log.error("fail to acquire lock when bidding",customException);
                throw customException;
            }
            log.info("락 획득 완료");
            // 경매중 인지 확인
            Auction findAuction = auctionRepository.findBidAuctionByAuctionIdWithFetchJoin(auctionId)
                    .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_BIDDING));

            //판매자는 입찰 불가능
            Product product = findAuction.getProduct();
            if (isProductSeller(product, loginId)) {
                throw new CustomException(AuctionHistoryErrorCode.NOT_BID_SELLER);
            }

            // 입찰자가 존재하는 회원인지
            Member member = memberRepository.findOneByLoginIdAndStatus(loginId, MemberStatus.EXIST)
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

            // 최근 입찰자 조회
            String latestMemberLoginId = auctionHistoryRepository.findLatestBidMemberLoginId(auctionId);
            // 입찰 가능 여부 확인
            checkBidAvailable(findAuction, bidPrice, latestMemberLoginId, loginId);

            // 현재 금액 변경
            findAuction.increaseNowPrice(bidPrice);
            // 입찰 기록 추가
            AuctionHistory auctionHistory = auctionHistoryRepository.save(createAuctionHistory(findAuction, bidPrice, member));
            bidResult = new AuctionBidResultDTO(bidPrice, findAuction.getProduct().getId(), auctionHistory.getId());

        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for lock for bidding ", e);
            Thread.currentThread().interrupt();
            throw new CustomException(AuctionErrorCode.TRY_AGAIN_BID);
        }finally {
            lock.unlock();
            log.info("락 반환 완료");

        }
        return bidResult;

    }


    @Transactional
    // 특정 횟수 이상 거래 거절시
    public Long banMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Long rejectCountByMemberId = auctionHistoryRepository.findRejectCountByMemberId(memberId);

        if (rejectCountByMemberId >= MEMBER_BAN_MIN_COUNT) {
            member.changeStatus(MemberStatus.DELETED);
        }

        log.info("ban memberId:{}, curCount={}, ban_min_count={}", memberId, rejectCountByMemberId, MEMBER_BAN_MIN_COUNT);
        return memberId;
    }

    private void checkBidAvailable(Auction auction, int bidPrice, String latestMemberLoginId, String loginId) {
        // 첫 입찰일 경우
        if (latestMemberLoginId == null) {
            // 현재 금액 <= 입찰 금액인지
            if (auction.getNowPrice() > bidPrice) {
                throw new CustomException(AuctionHistoryErrorCode.NO_HIGHER_THAN_NOW_PRICE);
            }
            // 입찰 단위가 맞는지
            validPriceUnit(bidPrice, auction);

        } else {// 첫 입찰이 아닌 경우
            // 현재 금액 < 입찰 금액
            if (auction.getNowPrice() >= bidPrice) {
                throw new CustomException(AuctionHistoryErrorCode.NO_HIGHER_THAN_NOW_PRICE);
            }
            // 입찰 단위가 맞는지
            validPriceUnit(bidPrice, auction);

            // 최근 입찰자와 현재 입찰자가 다른지
            if (latestMemberLoginId.equals(loginId)) {
                throw new CustomException(AuctionHistoryErrorCode.NOT_BID_BUYER);
            }
        }
    }

    private void validPriceUnit(int bidPrice, Auction auction) {
        if ((bidPrice - auction.getNowPrice()) % auction.getPriceUnit() != 0) {
            throw new CustomException(AuctionHistoryErrorCode.INVALID_PRICE_UNIT);
        }
    }

    private AuctionHistory createAuctionHistory(Auction auction, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .auction(auction)
                .member(member)
                .bidPrice(bidPrice)
                .build();
    }

    private boolean isProductSeller(Product product, String loginId) {
        return product.getMember().getLoginId().equals(loginId);
    }
}
