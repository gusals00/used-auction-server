package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerAndBuyerLoginIdDTO {

    private String sellerLoginId;
    private String buyerLoginId;

    @QueryProjection
    public SellerAndBuyerLoginIdDTO(String sellerLoginId, String buyerLoginId) {
        this.sellerLoginId = sellerLoginId;
        this.buyerLoginId = buyerLoginId;
    }
}
