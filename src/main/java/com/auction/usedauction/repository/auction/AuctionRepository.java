package com.auction.usedauction.repository.auction;

import com.auction.usedauction.domain.Auction;

import com.auction.usedauction.domain.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {

    boolean existsAuctionByIdAndStatus(Long actionId, AuctionStatus auctionStatus);

    Optional<Auction> findAuctionByIdAndStatus(Long auctionId, AuctionStatus auctionStatus);

    @Modifying
    @Query(value = "update auction a set a.status = :status where a.auction_id in (:auction_ids)", nativeQuery = true)
    int updateAuctionStatus(@Param("status") AuctionStatus auctionStatus, @Param("auction_ids") List<Long> auctionIdList);
}
