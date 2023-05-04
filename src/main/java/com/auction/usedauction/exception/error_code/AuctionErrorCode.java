package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {

    INVALID_DELETE_AUCTION_STATUS(HttpStatus.BAD_REQUEST, "입찰 상태일 경우에만 삭제가 가능합니다."),
    INVALID_UPDATE_AUCTION_STATUS(HttpStatus.BAD_REQUEST, "입찰 상태일 경우에만 수정이 가능합니다."),
    AUCTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "경매가 존재하지 않습니다."),
    AUCTION_NOT_BIDDING(HttpStatus.BAD_REQUEST, "경매중이 아닙니다"),
    INVALID_AUCTION(HttpStatus.BAD_REQUEST, "올바른 경매가 아닙니다"),
    INVALID_AUCTION_PRICE_UNIT(HttpStatus.BAD_REQUEST, "입찰 단위가 맞지 않습니다."),
    INVALID_CHANGE_TRANS(HttpStatus.BAD_REQUEST, "상태를 변경할 수 없습니다.");
    private final HttpStatus status;
    private final String message;
}
