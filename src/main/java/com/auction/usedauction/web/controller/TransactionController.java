package com.auction.usedauction.web.controller;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.TransStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.dto.SellerAndBuyerIdDTO;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.AuctionService;
import com.auction.usedauction.web.dto.MemberTransReq;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequestMapping("/api/trans")
@RequiredArgsConstructor
@Tag(name = "거래 확정 컨트롤러", description = "거래 확인 관련 api")
public class TransactionController {

    private final AuctionService auctionService;
    private final AuctionRepository auctionRepository;
    private final AuctionHistoryQueryRepository auctionHistoryQueryRepository;
    private final AuctionHistoryService auctionHistoryService;

    @PostMapping
    @Operation(summary = "거래 확정 요청 메서드")
    public ResultRes<MessageRes> changeSellerTransStatus(@Valid @RequestBody MemberTransReq memberTransReq, @AuthenticationPrincipal User user) {
        log.info("거래 확정 api 호출 auctionId={}, changeToStatus={}, loginId={}", memberTransReq.getAuctionId(), memberTransReq.getStatus(), user.getUsername());
        auctionService.memberTransCheck(memberTransReq.getAuctionId(), user.getUsername(), memberTransReq.getStatus());

        // 특정경매가 거래 실패일 경우
        boolean isTransFail = auctionRepository.existsAuctionByIdAndStatus(memberTransReq.getAuctionId(), AuctionStatus.TRANSACTION_FAIL);
        if (isTransFail) {
            SellerAndBuyerIdDTO sellerAndBuyerId = auctionHistoryQueryRepository.findSellerAndBuyerId(memberTransReq.getAuctionId())
                    .orElseThrow(() -> new CustomException(AuctionErrorCode.INVALID_AUCTION));
            // 판매자 구매자를 밴 해야 하는지 확인
            banUser(sellerAndBuyerId.getSellerId(), MemberType.Seller);
            banUser(sellerAndBuyerId.getBuyerId(), MemberType.Buyer);

        }
        return new ResultRes<>(new MessageRes("거래 확정되었습니다."));
    }

    private void banUser(Long memberId, MemberType memberType) {
        try {
            auctionHistoryService.banMember(memberId);
        } catch (CustomException e) {
            e.printStackTrace();
            // 판매자가 존재하지 않습니다 판매자 ID = {}
            // 구매자가 존재하지 않습니다 구매자 ID = {}
            log.error(memberType.name + "가 존재하지 않습니다. " + memberType.name + "= {}", memberId);
        }
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

    @Getter
    @AllArgsConstructor
    enum MemberType {
        Buyer("판매자"),
        Seller("구매자");

        private final String name;
    }
}
