package com.auction.usedauction.repository.auction_end;

import com.auction.usedauction.repository.dto.AuctionIdAndAuctionEndDateDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AuctionEndRepository {

    Long add(Long auctionId, LocalDateTime localDateTime);
    void add(List<AuctionIdAndAuctionEndDateDTO> idAndAuctionEndDateDTO);
    LocalDateTime findByAuctionId(Long auctionId);
    void clearAll();
}
