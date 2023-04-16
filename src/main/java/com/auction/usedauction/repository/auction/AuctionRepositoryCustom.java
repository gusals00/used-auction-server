package com.auction.usedauction.repository.auction;

import com.auction.usedauction.domain.Auction;

import java.util.Optional;

public interface AuctionRepositoryCustom {

    Optional<Auction> findBidAuctionByAuctionIdWithFetchJoin(Long auctionId);
}
