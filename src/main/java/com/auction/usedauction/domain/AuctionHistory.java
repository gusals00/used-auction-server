package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AuctionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_history_id")
    private Long id;

    private int bidPrice;

    @Enumerated(EnumType.STRING)
    private AuctionHistoryStatus status;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public AuctionHistory(int bidPrice, AuctionHistoryStatus status, User user, Product product) {
        this.bidPrice = bidPrice;
        this.status = status;
        this.user = user;
        this.product = product;
    }
}
