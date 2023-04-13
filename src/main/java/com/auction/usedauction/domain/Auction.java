package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    private int nowPrice;

    private int startPrice;

    private int priceUnit;

    private LocalDateTime auctionStartDate;

    private LocalDateTime auctionEndDate;

    @Enumerated(EnumType.STRING)
    private TransStatus sellerTransStatus;

    @Enumerated(EnumType.STRING)
    private TransStatus buyerTransStatus;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Builder
    public Auction(int startPrice, int priceUnit, LocalDateTime auctionEndDate) {
        this.startPrice = startPrice;
        this.priceUnit = priceUnit;
        this.auctionEndDate = auctionEndDate;
    }

    public void changeAuction(int startPrice, int priceUnit, LocalDateTime auctionEndDate) {
        this.startPrice = startPrice;
        this.priceUnit = priceUnit;
        this.auctionEndDate = auctionEndDate;
    }

    private void initTransStatus() {
        //판매자, 구매자 거래 확인 상태 초기값을 거래 전(TRANS_BEFORE)으로
        sellerTransStatus = TransStatus.TRANS_BEFORE;
        buyerTransStatus = TransStatus.TRANS_BEFORE;
    }

    private void initAuctionStatus() {
        status = AuctionStatus.BID;
    }

    private void initNowPrice() {
        nowPrice = startPrice;
    }

    @PrePersist
    private void initProductAndTransStatus() {
        initTransStatus();
        initStartDate();
        initNowPrice();
        initAuctionStatus();
    }

    private void initStartDate() {
        this.auctionStartDate = LocalDateTime.now();
    }

}
