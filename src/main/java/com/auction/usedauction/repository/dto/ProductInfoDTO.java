package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductInfoDTO {

    private Long productId;

    private String productName;

    private String sigImgSrc;

    private Integer endPrice;

    @QueryProjection
    public ProductInfoDTO(Long productId, String productName, String sigImgSrc, Integer endPrice) {
        this.productId = productId;
        this.productName = productName;
        this.sigImgSrc = sigImgSrc;
        this.endPrice = endPrice;
    }
}
