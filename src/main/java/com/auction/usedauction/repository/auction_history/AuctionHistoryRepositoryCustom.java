package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface AuctionHistoryRepositoryCustom {
    Page<AuctionHistory> findMyAuctionHistoryByCond(String loginId, MyPageSearchConReq cond, Pageable pageable);
    String findLatestBidMemberLoginId(Long auctionId);
    Long findRejectCountByMemberId(Long memberId);
    List<Long> findAuctionHistoryIdForChangeStatus(List<Long> auctionIds);
}
