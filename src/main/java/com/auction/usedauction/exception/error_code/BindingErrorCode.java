package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BindingErrorCode implements ErrorCode{

    INVALID_BINDING(HttpStatus.BAD_REQUEST,"제대로된 값을 입력해주세요.");

    private final HttpStatus status;
    private final String message;

}
