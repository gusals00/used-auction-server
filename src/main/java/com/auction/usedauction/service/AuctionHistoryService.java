package com.auction.usedauction.service;

import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionHistoryService {

    private final AuctionHistoryRepository auctionHistoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long biddingAuction(Long productId,int biddingPrice) {
        // 상품 상태가 BID(경매중) 인지

        // 판매자는 입찰 불가능
        // 최근 입찰자와 현재 입찰자가 다른지
        // 입찰 단위가 맞는지
        // 최근 가격보다 입찰 금액이 높은지
        // 입찰 기록 추가

        return 1L;
    }
}
