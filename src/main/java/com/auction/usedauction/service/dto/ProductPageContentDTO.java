package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ProductPageContentDTO {
    private String sellerLoginId;
    private String categoryName;
    private String productName;
    private Integer nowPrice;
    private String auctionEndDate;
    private String sigImgSrc;

    public ProductPageContentDTO(Product product) {
        this.sellerLoginId = product.getMember().getLoginId();
        this.categoryName = product.getCategory().getName();
        this.productName = product.getName();
        this.nowPrice = product.getNowPrice();
        this.auctionEndDate = product.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.sigImgSrc = product.getSigImage().getFullPath();
    }

}
