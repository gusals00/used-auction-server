package com.auction.usedauction.exception;

import com.auction.usedauction.exception.error_code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
@Builder
public class ErrorRes {

    private final HttpStatus status;
    private final String code;
    private final String msg;

    public ErrorRes(ErrorCode errorCode) {
        this.status = errorCode.getStatus();
        this.code = errorCode.name();
        this.msg = errorCode.getMessage();
    }

    public static ResponseEntity<ErrorRes> error(CustomException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorRes.builder()
                        .status(e.getErrorCode().getStatus())
                        .code(e.getErrorCode().name())
                        .msg(e.getErrorCode().getMessage())
                        .build());
    }

    public static ResponseEntity<ErrorRes> error(ErrorCode errorCode,String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorRes.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.name())
                        .msg(message)
                        .build());
    }

    public static ResponseEntity<ErrorRes> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorRes.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.name())
                        .msg(errorCode.getMessage())
                        .build());
    }
}
