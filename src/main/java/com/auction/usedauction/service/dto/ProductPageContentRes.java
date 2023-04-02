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
public class ProductPageContentRes {
    @Schema(description = "판매자로그인아이디",example = "hochang")
    private String sellerLoginId;
    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;
    @Schema(description = "상품 이름",example = "공학수학 책 팔아요")
    private String productName;
    @Schema(description = "현재가",example = "50000")
    private Integer nowPrice;
    @Schema(description = "경매 종료 날짜",example = "2023-10-12 12:01")
    private String auctionEndDate;
    @Schema(description = "대표이미지 src",example = "https://used.wsdf.wjfiojs.jpg")
    private String sigImgSrc;


    public ProductPageContentRes(Product product) {
        this.sellerLoginId = product.getMember().getLoginId();
        this.categoryName = product.getCategory().getName();
        this.productName = product.getName();
        this.nowPrice = product.getNowPrice();
        this.auctionEndDate = product.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.sigImgSrc = product.getSigImage().getFullPath();
    }

}
