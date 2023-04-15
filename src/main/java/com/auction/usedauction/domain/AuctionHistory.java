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
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Builder
    public AuctionHistory(int bidPrice, Member member, Auction auction) {
        this.bidPrice = bidPrice;
        this.member = member;
        this.auction = auction;
    }

    @PrePersist
    private void initStatus() {
        this.status = AuctionHistoryStatus.BID;
    }

    public void changeAuction(Auction auction) {
        if (auction != null) {
            this.auction = auction;
            auction.getAuctionHistoryList().add(this);
        }
    }
}
