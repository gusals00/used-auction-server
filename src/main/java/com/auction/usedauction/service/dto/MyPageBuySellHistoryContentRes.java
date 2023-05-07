package com.auction.usedauction.service.dto;


import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageBuySellHistoryContentRes {

    @Schema(description = "상품 ID",example = "1")
    private Long productId;

    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;

    @Schema(description = "상품 이름",example = "공학수학 책 팔아요")
    private String productName;

    @Schema(description = "낙찰가",example = "50000")
    private Integer nowPrice;

    @Schema(description = "상품 생성 날짜", example = "2023-10-12 12:01")
    private String createdDate;

    @Schema(description = "경매 종료 날짜",example = "2023-10-12 12:01")
    private String auctionEndDate;

    @Schema(description = "대표이미지 src",example = "https://used.wsdf.wjfiojs.jpg")
    private String sigImgSrc;

    @Schema(description = "경매 상태 - 거래성공, 거래실패",example = "거래 성공")
    private String status;

    public MyPageBuySellHistoryContentRes(Product product) {
        this.productId = product.getId();
        this.categoryName = product.getCategory().getName();
        this.productName = product.getName();
        this.nowPrice = product.getAuction().getNowPrice();
        this.createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.auctionEndDate = product.getAuction().getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.sigImgSrc = product.getSigImage().getFullPath();
        this.status = product.getAuction().getStatus().getDescription();
    }

    public MyPageBuySellHistoryContentRes(AuctionHistory auctionHistory) {
        this.productId = auctionHistory.getAuction().getProduct().getId();
        this.categoryName = auctionHistory.getAuction().getProduct().getCategory().getName();
        this.productName = auctionHistory.getAuction().getProduct().getName();
        this.nowPrice = auctionHistory.getAuction().getNowPrice();
        this.createdDate = auctionHistory.getAuction().getProduct().getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.auctionEndDate = auctionHistory.getAuction().getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.sigImgSrc = auctionHistory.getAuction().getProduct().getSigImage().getFullPath();
        this.status = auctionHistory.getAuction().getProduct().getAuction().getStatus().getDescription();
    }
}
