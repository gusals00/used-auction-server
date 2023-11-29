package com.auction.usedauction.web.controller;


import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.NotificationService;
import com.auction.usedauction.service.sseEmitter.SseEmitterService;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.service.dto.SseUpdatePriceDTO;
import com.auction.usedauction.web.dto.AuctionInfoRes;
import com.auction.usedauction.web.dto.BidReq;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
@Slf4j
@Tag(name = "bid controller", description = "입찰/낙찰 관련 api")
public class AuctionHistoryController {
    private final AuctionHistoryService auctionHistoryService;
    private final SseEmitterService sseEmitterService;
    private final AuctionRepository auctionRepository;
    private final NotificationService notificationService;

    
    @PostMapping("/{auctionId}")
    @Operation(summary = "상품 경매 입찰 메서드")
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
        try {
            //sse로 변경 가격 전달
            sseEmitterService.sendUpdatedBidPrice(new SseUpdatePriceDTO(auctionBidResult.getProductId(), nowPrice));
        } catch (TaskRejectedException e) {
            log.error("입찰 가격 전달 async thread pool 초과 예외 발생", e);
        }

        // 판매자에게 입찰 알림 전송
        notificationService.sendBidNotification(auctionBidResult.getProductId(), auctionBidResult.getSellerLoginId(), auctionBidResult.getProductName(), convertedPrice);

        return new ResultRes<>(new MessageRes(convertedPrice + "원 입찰을 성공했습니다."));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 경매 정보")
    public ResultRes<AuctionInfoRes> getAuctionInfo(@PathVariable Long productId) {
        Auction auction = auctionRepository.findAuctionByProductId(productId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));
        return new ResultRes<>(new AuctionInfoRes(auction));
    }
}
