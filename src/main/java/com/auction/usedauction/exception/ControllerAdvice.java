package com.auction.usedauction.exception;

import com.auction.usedauction.exception.error_code.BindingErrorCode;
import com.auction.usedauction.util.ValidatorMessageUtils;
import com.auction.usedauction.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    private final ValidatorMessageUtils validatorMessageUtils;

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ResultRes<ErrorRes>> handleCustomException(CustomException e) {
        log.error("[exception] class={}, code ={}, status = {}, message = {}",
                e.getErrorCode().getClass().getName(), e.getErrorCode().name(), e.getErrorCode().getStatus(), e.getErrorCode().getMessage(), e);
        return ErrorRes.error(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResultRes<ErrorRes>> BindException(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        log.error("[binding error]{}",bindingResult.toString());

        return ErrorRes.error(BindingErrorCode.INVALID_BINDING,validatorMessageUtils.getValidationMessage(bindingResult));
    }
}
