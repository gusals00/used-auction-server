package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.AuctionHistoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
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
    private final AuctionRepository auctionRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AuctionBidResultDTO biddingAuction(Long auctionId, int bidPrice, String loginId) {

        // 경매중 인지 확인,
        Auction findAuction = auctionRepository.findBidAuctionByAuctionIdWithFetchJoin(auctionId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_BIDDING));

        // 최근 입찰자 조회
        Optional<String> latestMemberLoginId = auctionHistoryRepository.findLatestBidMemberLoginId(auctionId);

        // 첫 입찰일 경우
        if (latestMemberLoginId.isEmpty()) {
            // 현재 금액 <= 입찰 금액인지, 입찰 단위가 맞는지
            if (findAuction.getNowPrice() > bidPrice || (bidPrice - findAuction.getNowPrice()) % findAuction.getPriceUnit() != 0) {
                throw new CustomException(AuctionHistoryErrorCode.AUCTION_FAIL);
            }
        } else {// 첫 입,찰이 아닌 경우
            // 현재 금액 < 입찰 금액, 입찰 단위가 맞는지
            if (findAuction.getNowPrice() >= bidPrice || (bidPrice - findAuction.getNowPrice()) % findAuction.getPriceUnit() != 0) {
                throw new CustomException(AuctionHistoryErrorCode.AUCTION_FAIL);
            }
            // 최근 입찰자와 현재 입찰자가 다른지
            if (latestMemberLoginId.get().equals(loginId)) {
                throw new CustomException(AuctionHistoryErrorCode.NOT_BID_BUYER);
            }
        }

        //판매자는 입찰 불가능
        if (findAuction.getProduct().getMember().getLoginId().equals(loginId)) {
            throw new CustomException(AuctionHistoryErrorCode.NOT_BID_SELLER);
        }

        // 입찰자가 존재하는 회원인지
        Member member = memberRepository.findOneByLoginIdAndStatus(loginId, MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));


        // 현재 금액 변경
        findAuction.increaseNowPrice(bidPrice);
        // 입찰 기록 추가
        AuctionHistory auctionHistory = auctionHistoryRepository.save(createAuctionHistory(findAuction, bidPrice, member));

        return new AuctionBidResultDTO(bidPrice, findAuction.getProduct().getId(), auctionHistory.getId());
    }

    private AuctionHistory createAuctionHistory(Auction auction, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .auction(auction)
                .member(member)
                .bidPrice(bidPrice)
                .build();
    }
}
