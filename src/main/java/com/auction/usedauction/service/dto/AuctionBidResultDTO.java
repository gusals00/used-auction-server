package com.auction.usedauction.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuctionBidResultDTO {

    private int nowPrice;
    private Long productId;
    private Long auctionHistoryId;


}
