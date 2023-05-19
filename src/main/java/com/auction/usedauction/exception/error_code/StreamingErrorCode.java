package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StreamingErrorCode implements ErrorCode {

    INVALID_SESSION(HttpStatus.NOT_FOUND, "방이 존재하지 않습니다"),
    STREAMING_END(HttpStatus.BAD_REQUEST, "방송이 이미 종료되었습니다."),
    STREAMING_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "다시 시도해 주세요"),
    INVALID_STREAMING_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 토큰입니다."),
    INVALID_STREAMING_PUBLISHER(HttpStatus.BAD_REQUEST, "스트리밍 PUBLISHER가 아닙니다."),
    INVALID_STREAMING_SUBSCRIBER(HttpStatus.BAD_REQUEST, "스트리밍 SUBSCRIBER가 아닙니다."),
    ALREADY_RECORDING(HttpStatus.BAD_REQUEST,"이미 녹화중입니다.");
    private final HttpStatus status;
    private final String message;
}
