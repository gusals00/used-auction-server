package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AuctionIdAndAuctionEndDateDTO {

    private Long AuctionId;
    private LocalDateTime endDate;

    @QueryProjection
    public AuctionIdAndAuctionEndDateDTO(Long auctionId, LocalDateTime endDate) {
        AuctionId = auctionId;
        this.endDate = endDate;
    }
}
