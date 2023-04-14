package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {

    INVALID_DELETE_AUCTION_STATUS_SUCCESSFUL_BID(HttpStatus.BAD_REQUEST, "낙찰 상태일때는 삭제할 수 없습니다."),
    INVALID_UPDATE_AUCTION_STATUS(HttpStatus.BAD_REQUEST, "입찰 상태일 경우에만 수정이 가능합니다.");

    private final HttpStatus status;
    private final String message;
}