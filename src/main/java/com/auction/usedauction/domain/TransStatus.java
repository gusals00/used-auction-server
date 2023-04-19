package com.auction.usedauction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransStatus {

    TRANS_COMPLETE("거래완료"), //거래완료
    TRANS_BEFORE("거래 전"), // 거래 전
    TRANS_REJECT("거래 불발"); //거래 불발

    private final String description;

}
