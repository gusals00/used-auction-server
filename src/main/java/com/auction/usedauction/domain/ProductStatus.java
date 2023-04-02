package com.auction.usedauction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    DELETED("삭제"), // 삭제
    BID("입찰중"), // 경매중
    SUCCESS_BID("낙찰 성공"), // 낙찰 성공
    FAIL_BID("낙찰 실패"), // 낙찰 실패
    TRANSACTION_OK("거래 성공"), // 거래 성공
    TRANSACTION_FAIL("거래 실패");

    private final String description;
}
