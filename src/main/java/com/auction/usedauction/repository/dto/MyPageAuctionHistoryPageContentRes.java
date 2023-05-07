package com.auction.usedauction.repository.dto;

import com.auction.usedauction.domain.AuctionHistory;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageAuctionHistoryPageContentRes {

    @Schema(description = "상품 ID",example = "1")
    private Long productId;

    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;

    @Schema(description = "상품 이름",example = "공학수학 책 팔아요")
    private String productName;

    @Schema(description = "대표이미지 src",example = "https://used.wsdf.wjfiojs.jpg")
    private String sigImgSrc;

    @Schema(description = "입찰/낙찰 가격", example = "50000")
    private Integer nowPrice;

    @Schema(description = "입찰/낙찰 날짜", example = "2023-10-12 12:01")
    private String createdDate;

    @Schema(description = "경매 종료 날짜", example = "2023-10-12 12:01")
    private String auctionEndDate;

    @Schema(description = "입찰/낙찰", example = "입찰")
    private String status;


    @QueryProjection
    public MyPageAuctionHistoryPageContentRes(AuctionHistory auctionHistory) {
        this.productId = auctionHistory.getAuction().getProduct().getId();
        this.categoryName = auctionHistory.getAuction().getProduct().getCategory().getName();
        this.productName =  auctionHistory.getAuction().getProduct().getName();
        this.nowPrice = auctionHistory.getBidPrice();
        this.createdDate = auctionHistory.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.auctionEndDate = auctionHistory.getAuction().getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.status = auctionHistory.getStatus().getDescription();
        this.sigImgSrc = auctionHistory.getAuction().getProduct().getSigImage().getFullPath();
    }

}
