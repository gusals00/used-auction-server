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

    private String sigImgSrc;

    private Integer endPrice;

    @QueryProjection
    public ProductInfoDTO(Long productId, String sigImgSrc, Integer endPrice) {
        this.productId = productId;
        this.sigImgSrc = sigImgSrc;
        this.endPrice = endPrice;
    }
}
