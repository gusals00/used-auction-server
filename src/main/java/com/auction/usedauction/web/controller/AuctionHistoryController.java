package com.auction.usedauction.web.controller;


import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class AuctionHistoryController {
    private final AuctionHistoryService auctionHistoryService;
    private final SimpMessageSendingOperations template;

    @PostMapping("/{auctionId}")
    public ResultRes<MessageRes> bidding(@PathVariable Long auctionId, @RequestBody @Valid @NotNull Integer biddingPrice, @AuthenticationPrincipal User user) {
        Long savedBidPrice = auctionHistoryService.biddingAuction(auctionId, biddingPrice, user.getUsername());

        return new ResultRes<>(new MessageRes(" 입찰을 성공했습니다."));
    }

}
