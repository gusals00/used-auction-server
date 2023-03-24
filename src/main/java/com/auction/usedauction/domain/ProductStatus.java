package com.auction.usedauction.domain;

public enum ProductStatus {
    EXIST, // 존재
    DELETED, // 삭제
    BID, // 경매중
    SUCCESS_BID, // 낙찰 성공
    FAIL_BID, // 낙찰 실패
    TRANSACTION // 거래 성공
}
