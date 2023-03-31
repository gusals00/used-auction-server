package com.auction.usedauction.exception.error_code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getMessage();
    HttpStatus getStatus();
    String name();
}
