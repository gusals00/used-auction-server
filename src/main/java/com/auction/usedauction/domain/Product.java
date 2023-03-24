package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    private String info;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private int buyNowPrice; // 즉시구매가 설정 안할 경우 -> -1

    private int nowPrice;

    private int startPrice;

    private int priceUnit;

    private int viewCount;

    private LocalDateTime auctionStartDate;

    private LocalDateTime auctionEndDate;

    private boolean sellerTransCheck;

    private boolean buyerTransCheck;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Product(String name, String info, ProductStatus status, int buyNowPrice, int nowPrice, int startPrice, int priceUnit, LocalDateTime auctionStartDate, LocalDateTime auctionEndDate, Category category, User user) {
        this.name = name;
        this.info = info;
        this.status = status;
        this.buyNowPrice = buyNowPrice;
        this.nowPrice = nowPrice;
        this.startPrice = startPrice;
        this.priceUnit = priceUnit;
        this.viewCount = 0;
        this.auctionStartDate = auctionStartDate;
        this.auctionEndDate = auctionEndDate;
        this.category = category;
        this.user = user;
        buyerTransCheck = false;
        sellerTransCheck = false;
    }
}
