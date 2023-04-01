package com.auction.usedauction.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductOrderCond {
    VIEW_ORDER("조회순"),
    NEW_PRODUCT_ORDER("최신 등록 순"),
    BID_CLOSING_ORDER("경매 마감 임박순"),
    HIGH_PRICE_ORDER("현재 가격 높은순"),
    LOW_PRICE_ORDER("현재 가격 낮은순");
    private final String descrpition;

}
