package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

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

//    @org.springframework.data.annotation.Version
//    private long version=0;

    @Enumerated(EnumType.STRING)
    private TransStatus sellerTransStatus;

    @Enumerated(EnumType.STRING)
    private TransStatus buyerTransStatus;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;
    @OneToOne(mappedBy = "auction", fetch = LAZY)
    private Product product;

    @OneToMany(mappedBy = "auction",fetch = LAZY)
    List<AuctionHistory> auctionHistoryList = new ArrayList<>();

    @Builder
    public Auction(int startPrice, int nowPrice, int priceUnit, LocalDateTime auctionEndDate) {
        this.startPrice = startPrice;
        this.nowPrice = nowPrice;
        this.priceUnit = priceUnit;
        this.auctionEndDate = auctionEndDate;
    }

    public void changeAuction(int startPrice, int priceUnit, LocalDateTime auctionEndDate) {
        this.startPrice = startPrice;
        this.nowPrice = startPrice;
        this.priceUnit = priceUnit;
        this.auctionEndDate = auctionEndDate;
    }

    public void setProduct(Product product) {
        this.product = product;
    }


    public void changeAuctionStatus(AuctionStatus auctionStatus) {
        this.status = auctionStatus;
    }
    public void changeSellerStatus(TransStatus transStatus) {
        this.sellerTransStatus = transStatus;
    }
    public void changeBuyerStatus(TransStatus transStatus) {
        this.buyerTransStatus = transStatus;
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
        if (nowPrice == 0) {
            nowPrice = startPrice;
        }
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

    public void increaseNowPrice(int price) {
        if (nowPrice <= price) {
            nowPrice = price;
        }
    }

    public void changeAuctionStartDate(LocalDateTime auctionStartDate) {
        this.auctionStartDate = auctionStartDate;
    }
}
