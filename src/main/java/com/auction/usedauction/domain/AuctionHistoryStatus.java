package com.auction.usedauction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionHistoryStatus {
    BID("입찰"), // 입찰
    SUCCESSFUL_BID("낙찰"); // 낙찰

    private final String description;
}
