package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BindingErrorCode implements ErrorCode{

    INVALID_BINDING(HttpStatus.BAD_REQUEST,"제대로된 값을 입력해주세요."),
    POSSIBLE_REGISTER_END_TIME(HttpStatus.BAD_REQUEST,"경매 종료 시간은 현재시간 기준 48시간 이후부터 선택해 주세요."),
    POSSIBLE_UPDATE_END_TIME(HttpStatus.BAD_REQUEST,"경매 종료 시간 수정은 현재시간 기준 4시간 이후로 선택해 주세요."),
    JSON_TYPE_MIS_MATCH_BINDING(HttpStatus.BAD_REQUEST,"올바른 값이 아닙니다.");
    private final HttpStatus status;
    private final String message;

}
