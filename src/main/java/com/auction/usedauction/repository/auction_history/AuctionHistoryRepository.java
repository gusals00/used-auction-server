package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.AuctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> , AuctionHistoryRepositoryCustom{

    int countByAuction(Auction auction);
}
