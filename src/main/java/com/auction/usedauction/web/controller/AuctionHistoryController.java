package com.auction.usedauction.web.controller;


import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
@Slf4j
public class AuctionHistoryController {
    private final AuctionHistoryService auctionHistoryService;
    private final SimpMessageSendingOperations template;

    @PostMapping("/{auctionId}")
    public ResultRes<MessageRes> bidding(@PathVariable Long auctionId, @RequestBody @Valid @NotNull Integer biddingPrice, @AuthenticationPrincipal User user) {
        log.info("입찰 시도 auctionId={}, bidPrice={}, buyerLoginId={}", auctionId, biddingPrice, user.getUsername());
        AuctionBidResultDTO nowPriceAndProductId = auctionHistoryService.biddingAuction(auctionId, biddingPrice, user.getUsername());

        log.info("변경된 현재 경매 가격 전달 nowPrice={}, path={}", nowPriceAndProductId.getNowPrice(), +nowPriceAndProductId.getProductId());
        template.convertAndSend("/sub/nowPrice/" + nowPriceAndProductId.getProductId(), nowPriceAndProductId.getNowPrice());
        return new ResultRes<>(new MessageRes(" 입찰을 성공했습니다."));
    }
}
