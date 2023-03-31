package com.auction.usedauction.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorRes> handleCustomException(CustomException e) {
        log.error("[exception] class={}, code ={}, status = {}, message = {}",
                e.getErrorCode().getClass().getName(), e.getErrorCode().name(), e.getErrorCode().getStatus(), e.getErrorCode().getMessage(), e);
        return ErrorRes.error(e);
    }


}
