package com.auction.usedauction.loadTest;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Dummy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dummy_id")
    private Long id;

    private Long memberId;
    private Long auctionId;
    private int bidPrice;

    @Builder
    public Dummy( Long memberId, Long auctionId, int bidPrice) {
        this.memberId = memberId;
        this.auctionId = auctionId;
        this.bidPrice = bidPrice;
    }
}
