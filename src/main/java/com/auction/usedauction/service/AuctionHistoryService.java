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
import com.auction.usedauction.repository.dto.AuctionIdAndBidCountDTO;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.util.LockKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.auction.usedauction.util.MemberBanConstants.*;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
@Validated
public class AuctionHistoryService {

    private final AuctionRepository auctionRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final AuctionHistoryQueryRepository auctionHistoryQueryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @RedissonLock(key = LockKey.BID_LOCK)
    public AuctionBidResultDTO biddingAuction(Long auctionId, int bidPrice, String loginId) {

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

        // 입찰 가능 여부 확인(금액, 가격)
        checkBidAvailable(findAuction, bidPrice, latestMemberLoginId, loginId);

        // 현재 금액 변경
        findAuction.increaseNowPrice(bidPrice);
        // 입찰 기록 추가
        AuctionHistory auctionHistory = auctionHistoryRepository.save(createAuctionHistory(findAuction, bidPrice, member));
        return new AuctionBidResultDTO(bidPrice, findAuction.getProduct().getId(), auctionHistory.getId());

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
        // 너무 큰 입찰가인 경우
        if (isHigherThanMaxPrice(auction.getNowPrice(), bidPrice)) {
            throw new CustomException(AuctionHistoryErrorCode.HIGHER_THAN_MAX_PRICE);
        }

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

    private boolean isHigherThanMaxPrice(int nowPrice, int bidPrice) {
        // 입찰가가 현재 금액의 2배보다 크면 입찰 최대 금액 초과한 것
        return nowPrice * 2 < bidPrice;
    }

    // 경매 종료시 경매 상태, 경매내역 상태 변경
    @Transactional
    public void changeAuctionStatusToAuctionEndStatuses(LocalDateTime localDateTime) {
        // 입찰이 종료된 경매 id, 입찰수 조회
        List<AuctionIdAndBidCountDTO> idAndBIdCounts = auctionHistoryQueryRepository.findIdAndBidCountListByStatusAndEndDate(AuctionStatus.BID, localDateTime);

        // 경매 상태를 FAIL_BID(낙찰 실패) 로 변경
        List<Long> failBidIds = getFailBidIds(idAndBIdCounts);
        changeAuctionStatusAfterAuctionEnd(AuctionStatus.FAIL_BID, failBidIds);

        // 경매 상태를 SUCCESS_BID(낙찰 성공) 로 변경
        List<Long> successBidIds = getSuccessBidIds(idAndBIdCounts);
        changeAuctionStatusAfterAuctionEnd(AuctionStatus.SUCCESS_BID, successBidIds);

        int changeHistoryCount = 0;
        // 최근 입찰 내역들을 낙찰 상태로 변경
        if (successBidIds.size() > 0) {
            List<Long> auctionHistoryIds = auctionHistoryRepository.findAuctionHistoryIdForChangeStatus(successBidIds);
            changeHistoryCount = auctionHistoryRepository.updateAuctionHistoryStatus(AuctionHistoryStatus.SUCCESSFUL_BID, auctionHistoryIds);
        }

        log.info("경매내역 상태 낙찰로 변경. 변경 개수 = {}", changeHistoryCount);
    }

    //경매 종료시 경매 상태 변경
    private void changeAuctionStatusAfterAuctionEnd(AuctionStatus status, List<Long> auctionIds) {
        int changeCount = 0;
        if (auctionIds.size() > 0) {
            changeCount = auctionRepository.updateAuctionStatus(status, auctionIds);
        }
        // 낙찰 성공 or 낙찰 실패 상태로 변경 변경 개수 = 2
        log.info("{} 상태로 변경. 변경 개수 = {}", status.getDescription(), changeCount);
    }

    // 낙찰 실패로 변경할 auctionIds
    private List<Long> getFailBidIds(List<AuctionIdAndBidCountDTO> idAndBIdCounts) {
        return idAndBIdCounts.stream().filter(auctionIdAndBidCountDTO -> auctionIdAndBidCountDTO.getCount() == 0)
                .map(AuctionIdAndBidCountDTO::getAuctionId)
                .collect(Collectors.toList());
    }

    // 낙찰 성공 상태로 변경할 auctionIds
    private List<Long> getSuccessBidIds(List<AuctionIdAndBidCountDTO> idAndBIdCounts) {
        return idAndBIdCounts.stream().filter(auctionIdAndBidCountDTO -> auctionIdAndBidCountDTO.getCount() > 0)
                .map(AuctionIdAndBidCountDTO::getAuctionId)
                .collect(Collectors.toList());
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
