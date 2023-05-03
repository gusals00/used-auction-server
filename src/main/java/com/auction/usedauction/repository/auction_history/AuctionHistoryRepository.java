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

    @Modifying(clearAutomatically = true)
    @Query(value = "update auction_history ah set ah.status = :#{#status.name()} where ah.auction_history_id in (:auction_history_ids)", nativeQuery = true)
    int updateAuctionHistoryStatus(@Param("status") AuctionHistoryStatus auctionHistoryStatus, @Param("auction_history_ids") List<Long> auctionHistoryIdList);

    // SUCCESSFUL_BID 상태로 변경할 경매내역 id 리스트 조회
    @Query(value = "select ah.auction_history_id" +
                    " from (select ah.auction_id as auction_id, max(ah.bid_price) as max_price" +
                    "      from auction_history ah" +
                    "      where ah.auction_id in (:auctionIds)" +
                    "      group by ah.auction_id) ah_max," +
                    " auction_history ah" +
                    " where ah_max.auction_id = ah.auction_id and ah.bid_price = ah_max.max_price", nativeQuery = true)
    List<Long> findAuctionHistoryIdForChangeStatus(@Param("auctionIds") List<Long> auctionIds);

}
