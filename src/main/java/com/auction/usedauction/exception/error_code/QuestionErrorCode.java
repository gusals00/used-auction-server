package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuestionErrorCode implements ErrorCode {

    QUESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 질문을 찾을 수 없습니다."),
    QUESTION_WRITE_SELLER_ONLY(HttpStatus.BAD_REQUEST,"대댓글은 판매자만 작성할 수 있습니다."),
    INVALID_LAYER_QUESTION(HttpStatus.BAD_REQUEST,"대댓글까지만 작성이 가능합니다");
    private final HttpStatus status;
    private final String message;
}
