package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {


    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST,"해당 상품을 찾을 수 없습니다."),
    INVALID_DELETE_PRODUCT_STATUS(HttpStatus.BAD_REQUEST,"해당 상품을 삭제할 수 있는 상태가 아닙니다."),

    INVALID_DELETE_PRODUCT_HISTORY(HttpStatus.BAD_REQUEST, "입찰 기록이 있으면 상품을 삭제할 수 없습니다"),
    INVALID_UPDATE_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "해당 상품을 수정할 수 있는 상태가 아닙니다."),
    INVALID_UPDATE_PRODUCT_HISTORY(HttpStatus.BAD_REQUEST, "입찰 기록이 있으면 상품을 수정할 수 없습니다");

    private final HttpStatus status;
    private final String message;
}
