package com.auction.usedauction.repository.auction;

import com.auction.usedauction.domain.Auction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepositoryCustom {

    Optional<Auction> findBidAuctionByAuctionIdWithFetchJoin(Long auctionId);

    List<Long> findSuccessButNotTransIdByDate(LocalDateTime date);
    Optional<Auction> findAuctionByProductId(Long productId);
}
