package com.auction.usedauction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    SELLER_TRANS_CONFIRM("판매자 거래확정"),
    BUYER_TRANS_CONFIRM("구매자 거래확정"),
    BID("입찰");

    private final String description;
}
