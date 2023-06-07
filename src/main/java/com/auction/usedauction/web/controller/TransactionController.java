package com.auction.usedauction.web.controller;

import com.auction.usedauction.domain.TransStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.AuctionService;
import com.auction.usedauction.web.dto.MemberTransReq;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static com.auction.usedauction.exception.error_code.ProductErrorCode.*;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequestMapping("/api/trans")
@RequiredArgsConstructor
@Tag(name = "Transaction confirm controller", description = "거래 확정 관련 api")
public class TransactionController {

    private final AuctionService auctionService;
    private final AuctionHistoryService auctionHistoryService;
    private final ProductRepository productRepository;

    @PostMapping
    @Operation(summary = "거래 확정 요청 메서드")
    public ResultRes<MessageRes> changeSellerTransStatus(@Valid @RequestBody MemberTransReq memberTransReq, @AuthenticationPrincipal User user) {
        Long auctionId = productRepository.findById(memberTransReq.getProductId())
                .orElseThrow(() -> new CustomException(PRODUCT_NOT_FOUND)).getAuction().getId();

        log.info("거래 확정 api 호출 auctionId={}, changeToStatus={}, loginId={}", auctionId, memberTransReq.getStatus(), user.getUsername());
        auctionService.memberTransConfirm(auctionId, user.getUsername(), memberTransReq.getStatus());

        // 경매 id로 ban 확인 후 ban
        auctionHistoryService.banMemberByAuctionId(auctionId);
        return new ResultRes<>(new MessageRes("거래 확정되었습니다."));
    }

    @GetMapping("/status")
    @Operation(summary = "거래 확정시 거래 상태 기준")
    public ResultRes<List<StatusTypeRes>> getTransStatus() {
        return new ResultRes<>(Arrays
                .stream(TransStatus.values())
                .filter(transStatus -> transStatus != TransStatus.TRANS_BEFORE)
                .map(StatusTypeRes::new)
                .collect(toList()));
    }

    @Getter
    @Setter
    static class StatusTypeRes {
        @Schema(description = "상태 타입", example = "TRANS_BEFORE")
        private String name;
        @Schema(description = "설명", example = "거래 불발")
        private String description;

        public StatusTypeRes(TransStatus type) {
            this.name = type.name();
            this.description = type.getDescription();
        }
    }
}
