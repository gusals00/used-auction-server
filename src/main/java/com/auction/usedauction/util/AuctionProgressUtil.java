package com.auction.usedauction.util;

import com.auction.usedauction.domain.AuctionStatus;

public class AuctionProgressUtil {
    private static final String UNDER_AUCTION = "경매중";
    private static final String END_AUCTION = "경매 종료";

    public static String changeAuctionStatusToName(AuctionStatus auctionStatus) {
        if (auctionStatus == AuctionStatus.BID) {
            return UNDER_AUCTION;
        } else {
            return END_AUCTION;
        }
    }
}
