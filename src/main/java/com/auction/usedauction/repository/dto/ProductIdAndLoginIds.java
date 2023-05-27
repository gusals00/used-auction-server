package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductIdAndLoginIds {

    private Long productId;
    private String sellerLoginId;
    private String buyerLoginId;
    private String productName;

    @QueryProjection
    public ProductIdAndLoginIds(Long productId, String sellerLoginId, String buyerLoginId, String productName) {
        this.productId = productId;
        this.sellerLoginId = sellerLoginId;
        this.buyerLoginId = buyerLoginId;
        this.productName = productName;
    }
}
