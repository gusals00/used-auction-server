package com.auction.usedauction.repository.auction;

import com.auction.usedauction.domain.Auction;

import com.auction.usedauction.domain.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {

    boolean existsAuctionByIdAndStatus(Long actionId,AuctionStatus auctionStatus);
}
