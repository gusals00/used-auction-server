package com.auction.usedauction.repository.auction_history;

import java.util.Optional;

public interface AuctionHistoryRepositoryCustom {

    Optional<String> findLatestBidMemberLoginId(Long auctionId);
}
