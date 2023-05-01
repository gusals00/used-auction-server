package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageProductPageContentRes {

    @Schema(description = "상품 ID",example = "1")
    private Long productId;

    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;

    @Schema(description = "상품 이름",example = "공학수학 책 팔아요")
    private String productName;

    @Schema(description = "현재가",example = "50000")
    private Integer nowPrice;

    @Schema(description = "상품 생성 날짜", example = "2023-10-12 12:01")
    private String createdDate;

    @Schema(description = "경매 종료 날짜",example = "2023-10-12 12:01")
    private String auctionEndDate;

    @Schema(description = "대표이미지 src",example = "https://used.wsdf.wjfiojs.jpg")
    private String sigImgSrc;

    @Schema(description = "경매 상태 - 경매중, 낙찰 등",example = "경매중")
    private String status;

    @Schema(description = "상품 수정 가능한 상태인가", example = "true")
    private boolean isPossibleUpdate;

    public MyPageProductPageContentRes(Product product,boolean isPossibleUpdate) {
        this.productId = product.getId();
        this.categoryName = product.getCategory().getName();
        this.productName = product.getName();
        this.nowPrice = product.getAuction().getNowPrice();
        this.createdDate = product.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.auctionEndDate = product.getAuction().getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.sigImgSrc = product.getSigImage().getFullPath();
        this.status = product.getAuction().getStatus().getDescription();
        this.isPossibleUpdate = isPossibleUpdate;
    }
}
