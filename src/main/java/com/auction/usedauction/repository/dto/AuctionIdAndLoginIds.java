package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuctionIdAndLoginIds {

    private Long auctionId;
    private String sellerLoginId;
    private String buyerLoginId;

    @QueryProjection
    public AuctionIdAndLoginIds(Long auctionId,String sellerLoginId, String buyerLoginId) {
        this.auctionId = auctionId;
        this.sellerLoginId = sellerLoginId;
        this.buyerLoginId = buyerLoginId;
    }
}
