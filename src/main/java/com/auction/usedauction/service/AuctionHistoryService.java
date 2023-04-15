package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.AuctionHistoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionHistoryService {

    private final AuctionHistoryQueryRepository auctionHistoryQueryRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final MemberRepository memberRepository;
    @Transactional
    public Long biddingAuction(Long auctionId, int bidPrice, String loginId) {

        // 경매중 인지 확인
        AuctionHistory findAuctionHistory = auctionHistoryQueryRepository.findLatestAuctionHistoryByAuctionId(auctionId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_BIDDING));
        Auction auction = findAuctionHistory.getAuction();

        //판매자는 입찰 불가능
        if (auction.getProduct().getMember().getLoginId().equals(loginId)) {
            throw new CustomException(AuctionHistoryErrorCode.NOT_BID_SELLER);
        }
        Member member = memberRepository.findOneByLoginIdAndStatus(loginId, MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        //최근 입찰자와 현재 입찰자가 다른지
        if (findAuctionHistory.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(AuctionHistoryErrorCode.NOT_BID_BUYER);
        }
        // 현재 금액보다 입찰 금액이 높은지, 입찰 단위가 맞는지
        if (auction.getNowPrice() >= bidPrice || (bidPrice - auction.getNowPrice()) % auction.getPriceUnit() != 0) {
            throw new CustomException(AuctionHistoryErrorCode.AUCTION_FAIL);
        }

        // 현재 금액 변경
        auction.increaseNowPrice(bidPrice);
        // 입찰 기록 추가
        auctionHistoryRepository.save(createAuctionHistory(auction, bidPrice, member));
        return 1L;
    }

    private AuctionHistory createAuctionHistory(Auction auction, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .auction(auction)
                .member(member)
                .bidPrice(bidPrice)
                .build();
    }
}
