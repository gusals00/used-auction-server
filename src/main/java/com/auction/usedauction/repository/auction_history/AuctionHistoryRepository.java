package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.AuctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long>, AuctionHistoryRepositoryCustom {

    List<AuctionHistory> findAllByAuction(Auction auction);
    int countByAuction(Auction auction);
}
