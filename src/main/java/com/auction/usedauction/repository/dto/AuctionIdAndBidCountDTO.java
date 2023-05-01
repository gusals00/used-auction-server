package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class AuctionIdAndBidCountDTO {

    private long count;
    private Long auctionId;

    @QueryProjection
    public AuctionIdAndBidCountDTO(long  count, Long auctionId) {
       this.count = count;
       this.auctionId = auctionId;
    }
}
