package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LockErrorCode implements ErrorCode {

    TRY_AGAIN_LOCK(HttpStatus.BAD_REQUEST, "다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}
