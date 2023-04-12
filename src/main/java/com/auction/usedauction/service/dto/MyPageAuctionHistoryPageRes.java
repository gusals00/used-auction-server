package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.AuctionHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MyPageAuctionHistoryPageRes {

    private List<MyPageAuctionHistoryPageContentRes> myPageAuctionHistoryPageContents;
    private Page<AuctionHistory> page;
}
