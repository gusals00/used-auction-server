package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AuctionHistoryRepositoryCustom {
    Page<AuctionHistory> findMyAuctionHistoryByCond(String loginId, MyPageSearchConReq cond, Pageable pageable);
    Optional<String> findLatestBidMemberLoginId(Long auctionId);
}
