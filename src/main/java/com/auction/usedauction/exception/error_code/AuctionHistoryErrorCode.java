package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionHistoryErrorCode implements ErrorCode{

    AUCTION_FAIL(HttpStatus.BAD_REQUEST, "입찰에 실패했습니다."),
    NOT_BID_SELLER(HttpStatus.BAD_REQUEST, "판매자는 입찰이 불가능합니다."),
    NOT_BID_BUYER(HttpStatus.BAD_REQUEST, "연속 2번 입찰은 불가능합니다."),

    EXIST_AUCTION_HISTORY(HttpStatus.BAD_REQUEST, "입찰 내역이 존재합니다.");
    private final HttpStatus status;
    private final String message;

}
