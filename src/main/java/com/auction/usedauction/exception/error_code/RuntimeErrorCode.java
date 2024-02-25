package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum RuntimeErrorCode implements ErrorCode{
    RUNTIME_ERROR_CODE(HttpStatus.INTERNAL_SERVER_ERROR, "다시 시도해주세요");

    private final HttpStatus status;
    private final String message;
}