package com.auction.usedauction.web.controller;


import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.web.dto.BidReq;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
@Slf4j
@Tag(name = "입찰/낙찰 컨트롤러", description = "입찰/낙찰 관련 api")
public class AuctionHistoryController {
    private final AuctionHistoryService auctionHistoryService;
    private final SimpMessageSendingOperations template;

    @PostMapping("/{auctionId}")
    @Operation(summary = "카테고리 리스트 조회 메서드")
    public ResultRes<MessageRes> bidding(@PathVariable Long auctionId,
                                         @RequestBody @Valid BidReq bidReq,
                                         @AuthenticationPrincipal User user,
                                         Locale locale) {
        log.info("입찰 시도 auctionId={}, bidPrice={}, buyerLoginId={}", auctionId, bidReq.getBidPrice(), user.getUsername());
        AuctionBidResultDTO auctionBidResult = auctionHistoryService.biddingAuction(auctionId, bidReq.getBidPrice(), user.getUsername());

        int nowPrice = auctionBidResult.getNowPrice();
        String subscribeUrl = "/sub/nowPrice/" + auctionBidResult.getProductId();
        String convertedPrice = NumberFormat.getInstance(locale).format(nowPrice);

        log.info("변경된 현재 경매 가격 전달 nowPrice={}, actionHistoryId={} ,path={}",nowPrice , auctionBidResult.getAuctionHistoryId(), subscribeUrl);
        template.convertAndSend(subscribeUrl, nowPrice);

        return new ResultRes<>(new MessageRes(convertedPrice+"원 입찰을 성공했습니다."));
    }
}
