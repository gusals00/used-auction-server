package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_USER(HttpStatus.BAD_REQUEST, "올바르지 않은 사용자입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "사용자가 이미 존재합니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    EMAIL_AUTH_FAIL(HttpStatus.BAD_REQUEST, "이메일 인증 실패");

    private final HttpStatus status;
    private final String message;
}
