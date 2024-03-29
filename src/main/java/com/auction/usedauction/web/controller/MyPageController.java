package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.dto.MyPageAuctionHistoryPageContentRes;
import com.auction.usedauction.repository.dto.TransactionCountDTO;
import com.auction.usedauction.repository.query.ProductQueryRepository;
import com.auction.usedauction.service.MemberService;
import com.auction.usedauction.service.dto.*;
import com.auction.usedauction.service.query.MyPageQueryService;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@Tag(name = "mypage controller", description = "마이페이지 관련 api")
public class MyPageController {

     private final MemberService memberService;
     private final MyPageQueryService myPageQueryService;
     private final ProductQueryRepository productQueryRepository;
     private final AuctionHistoryRepository auctionHistoryRepository;

    @Operation(summary = "회원정보 조회")
     @GetMapping
    public ResultRes<MemberDetailInfoRes> getInfo(@AuthenticationPrincipal User user) {
         return new ResultRes(memberService.getInfo(user.getUsername()));
     }

    @Operation(summary = "회원정보 수정")
     @PatchMapping
    public ResultRes<MessageRes> updateInfo(@AuthenticationPrincipal User user, @RequestBody @Valid UserUpdateReq userUpdateReq) {
         memberService.updateInfo(user.getUsername(), userUpdateReq);

         return new ResultRes(new MessageRes("회원정보 수정 성공"));
     }

     @Operation(summary = "상품 관리")
    @GetMapping("/products")
    public PageListRes<MyPageProductPageContentRes> getMyProducts(@AuthenticationPrincipal User user, @ParameterObject @Valid MyPageSearchConReq searchConReq) {
         return myPageQueryService.getMyProductPage(searchConReq, user.getUsername());
     }

     @Operation(summary = "입찰/낙찰 내역")
    @GetMapping("/auction-history")
    public PageListRes<MyPageAuctionHistoryPageContentRes> getMyAuctionHistory(@AuthenticationPrincipal User user, @ParameterObject @Valid MyPageSearchConReq searchConReq) {
        return myPageQueryService.getMyAuctionHistoryPage(searchConReq, user.getUsername());
     }

    @Operation(summary = "구매 내역")
    @GetMapping("buy-history")
    public PageListRes<MyPageBuySellHistoryContentRes> getMyBuyHistory(@AuthenticationPrincipal User user, @ParameterObject @Valid MyPageSearchConReq searchConReq) {
        return myPageQueryService.getMyBuyHistoryPage(searchConReq, user.getUsername());
    }

    @Operation(summary = "판매 내역")
    @GetMapping("sales-history")
    public PageListRes<MyPageBuySellHistoryContentRes> getMySalesHistory(@AuthenticationPrincipal User user, @ParameterObject @Valid MyPageSearchConReq searchConReq) {
        return myPageQueryService.getMySalesHistoryPage(searchConReq, user.getUsername());
    }

    @Operation(summary = "판매횟수, 경고횟수 조회")
    @GetMapping("/count")
    public TransactionAndRejectCountRes getTransactionCount(@AuthenticationPrincipal User user) {
        TransactionCountDTO transactionCountDTO = productQueryRepository.findTransactionCountByLoginId(user.getUsername());
        Long rejectCount = auctionHistoryRepository.findRejectCountByMemberLoginId(user.getUsername());

        return new TransactionAndRejectCountRes(transactionCountDTO.getAllCount(), transactionCountDTO.getAllCount(), rejectCount);
    }
}
