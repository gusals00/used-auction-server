package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.AuctionHistoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long>, AuctionHistoryRepositoryCustom {

    List<AuctionHistory> findAllByAuction(Auction auction);

    int countByAuction(Auction auction);

    @Modifying
    @Query(value = "update auction_history ah set ah.status = :status where ah.auction_history_id in (:auction_history_ids)", nativeQuery = true)
    int updateAuctionHistoryStatus(@Param("status") AuctionHistoryStatus auctionHistoryStatus, @Param("auction_history_ids") List<Long> auctionHistoryIdList);
}
