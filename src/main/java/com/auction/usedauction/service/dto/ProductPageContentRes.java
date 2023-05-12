package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.util.AuctionProgressUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ProductPageContentRes {
    @Schema(description = "판매자 별명",example = "hochang123")
    private String nickname;
    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;
    @Schema(description = "상품 이름",example = "공학수학 책 팔아요")
    private String productName;
    @Schema(description = "상품 ID",example = "1")
    private Long productId;
    @Schema(description = "현재가",example = "50000")
    private Integer nowPrice;
    @Schema(description = "경매 종료 날짜",example = "2023-10-12 12:01")
    private String auctionEndDate;
    @Schema(description = "대표이미지 src",example = "https://used.wsdf.wjfiojs.jpg")
    private String sigImgSrc;
    @Schema(description = "경매 상태",example = "경매 종료")
    private String status;
    @Schema(description = "경매 ID",example = "1")
    private Long auctionId;
    @Schema(description = "방송중인지",example = "true")
    private boolean liveBroadcasting;

    public ProductPageContentRes(Product product,boolean isLive) {
        this.nickname = product.getMember().getName();
        this.categoryName = product.getCategory().getName();
        this.productName = product.getName();
        this.productId = product.getId();

        Auction auction = product.getAuction();
        this.nowPrice = auction.getNowPrice();
        this.auctionEndDate = auction.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.sigImgSrc = product.getSigImage().getFullPath();
        this.status = AuctionProgressUtil.changeAuctionStatusToName(auction.getStatus());
        this.auctionId = product.getAuction().getId();
        // 방송중인지
        this.liveBroadcasting = isLive;
    }

}
