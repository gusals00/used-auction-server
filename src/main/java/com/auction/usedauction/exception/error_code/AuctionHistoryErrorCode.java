package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionHistoryErrorCode implements ErrorCode{
    AUCTION_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "입찰 기록이 없습니다."),
    NO_HIGHER_THAN_NOW_PRICE(HttpStatus.BAD_REQUEST, "입찰 가격이 현재 가격보다 작습니다."),
    INVALID_PRICE_UNIT(HttpStatus.BAD_REQUEST, "단위 가격이 올바르지 않습니다."),
    NOT_BID_SELLER(HttpStatus.BAD_REQUEST, "판매자는 입찰이 불가능합니다."),
    NOT_BID_BUYER(HttpStatus.BAD_REQUEST, "연속 2번 입찰은 불가능합니다."),

    EXIST_AUCTION_HISTORY(HttpStatus.BAD_REQUEST, "입찰 내역이 존재합니다.");
    private final HttpStatus status;
    private final String message;

}
