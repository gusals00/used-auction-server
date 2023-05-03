package com.auction.usedauction.exception;

import com.auction.usedauction.exception.error_code.BindingErrorCode;
import com.auction.usedauction.util.ValidatorMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    public ResponseEntity<ErrorRes> handleCustomException(CustomException e) {
        log.error("[exception] class={}, code ={}, status = {}, message = {}",
                e.getErrorCode().getClass().getName(), e.getErrorCode().name(), e.getErrorCode().getStatus(), e.getErrorCode().getMessage(), e);
        return ErrorRes.error(e);
    }

    // JSON이 올바른 형식이 아닌 경우
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRes> jsonDeserializeException(HttpMessageNotReadableException e) {
        log.error("[exception] 올바른 JSON parsing이 불가능합니다. 올바른 JSON 을 넘겨주세요", e);
        return ErrorRes.error(BindingErrorCode.JSON_TYPE_MIS_MATCH_BINDING);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorRes> BindException(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        log.error("[binding error]{}",bindingResult.toString());

        return ErrorRes.error(BindingErrorCode.INVALID_BINDING,validatorMessageUtils.getValidationMessage(bindingResult));
    }
}
