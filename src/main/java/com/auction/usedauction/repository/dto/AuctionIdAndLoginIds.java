package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuctionIdAndLoginIds {

    private Long productId;
    private String sellerLoginId;
    private String buyerLoginId;

    @QueryProjection
    public AuctionIdAndLoginIds(Long productId, String sellerLoginId, String buyerLoginId) {
        this.productId = productId;
        this.sellerLoginId = sellerLoginId;
        this.buyerLoginId = buyerLoginId;
    }
}
