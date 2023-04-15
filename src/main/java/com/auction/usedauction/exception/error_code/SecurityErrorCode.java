package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SecurityErrorCode implements ErrorCode {

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 실패"),
    WRONG_TYPE_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 JWT 서명입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 입니다."),
    WRONG_TOKEN(HttpStatus.UNAUTHORIZED, "JWT가 잘못되었습니다."),
    LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃 처리된 JWT 입니다.");

    private final HttpStatus status;
    private final String message;
}
