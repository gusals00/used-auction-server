package com.auction.usedauction.web.dto;

import com.auction.usedauction.domain.Auction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class AuctionInfoRes {

    @Schema(description = "경매 ID", example = "1")
    private Long auctionId;
    @Schema(description = "경매 종료 날짜", example = "2023-10-12 12:01")
    private String auctionEndDate;
    @Schema(description = "현재 가격", example = "10000")
    private int nowPrice;
    @Schema(description = "입찰 단위 가격", example = "1000")
    private int priceUnit;

    public AuctionInfoRes(Auction auction) {
        auctionId = auction.getId();
        auctionEndDate = auction.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        nowPrice = auction.getNowPrice();
        priceUnit = auction.getPriceUnit();
    }
}
