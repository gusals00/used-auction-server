package com.auction.usedauction.web.controller;


import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.SseEmitterService;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.web.dto.BidReq;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final SseEmitterService sseEmitterService;

    @PostMapping("/{auctionId}")
    @Operation(summary = "상품 경매 메서드")
    public ResultRes<MessageRes> bidding(@PathVariable Long auctionId,
                                         @RequestBody @Valid BidReq bidReq,
                                         @AuthenticationPrincipal User user,
                                         Locale locale) {
        log.info("입찰 시도 auctionId={}, bidPrice={}, buyerLoginId={}", auctionId, bidReq.getBidPrice(), user.getUsername());
        AuctionBidResultDTO auctionBidResult = auctionHistoryService.biddingAuction(auctionId, bidReq.getBidPrice(), user.getUsername());

        int nowPrice = auctionBidResult.getNowPrice();
        String convertedPrice = NumberFormat.getInstance(locale).format(nowPrice);

        log.info("변경된 현재 경매 가격 전달 nowPrice={}, actionHistoryId = {}, actionHistoryId = {}",
                nowPrice, auctionBidResult.getAuctionHistoryId(), auctionBidResult.getProductId());
        //sse로 변경 가격 전달
        sseEmitterService.sendUpdatedBidPriceByProductId(auctionBidResult.getProductId(), nowPrice);

        return new ResultRes<>(new MessageRes(convertedPrice + "원 입찰을 성공했습니다."));
    }
}
