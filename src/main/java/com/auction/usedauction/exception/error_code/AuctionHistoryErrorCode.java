package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionHistoryErrorCode implements ErrorCode{

    EXIST_AUCTION_HISTORY(HttpStatus.BAD_REQUEST, "입찰 내역이 존재합니다.");

    private final HttpStatus status;
    private final String message;

}
