package com.auction.usedauction.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LockKey {
    BID_LOCK( "입찰 redisson 락");

    private final String description;
}
