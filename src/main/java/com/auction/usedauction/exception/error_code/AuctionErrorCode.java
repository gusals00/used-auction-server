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
    INVALID_CHANGE_TRANS(HttpStatus.BAD_REQUEST, "해당 상태로 변경이 불가능합니다."),
    INVALID_AUCTION_CHANGE_TRANS(HttpStatus.BAD_REQUEST, "거래 확정이 불가능합니다."),
    ALREADY_AUCTION_TRANS_COMPLETE(HttpStatus.BAD_REQUEST, "거래 확정이 이미 종료되었습니다."),
    ALREADY_USER_CHANGE_TRANS(HttpStatus.BAD_REQUEST, "사용자가 이미 거래 확정을 했습니다.");
    private final HttpStatus status;
    private final String message;
}
