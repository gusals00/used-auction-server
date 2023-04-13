package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private void initTransStatus() {
        //판매자, 구매자 거래 확인 상태 초기값을 거래 전(TRANS_BEFORE)으로
        sellerTransStatus = TransStatus.TRANS_BEFORE;
        buyerTransStatus = TransStatus.TRANS_BEFORE;
    }

    @PrePersist
    private void initProductAndTransStatus() {
        initTransStatus();
        initStartDate();
    }

    private void initStartDate() {
        this.auctionStartDate = LocalDateTime.now();
    }

}
