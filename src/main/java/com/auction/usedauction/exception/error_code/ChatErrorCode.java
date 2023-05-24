package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorCode implements ErrorCode{
    CHAT_ROOM_DUPLICATED(HttpStatus.BAD_REQUEST, "채팅방이 이미 존재합니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 채팅방을 찾을 수 없습니다."),
    INVALID_ROOM_ID(HttpStatus.BAD_REQUEST, "잘못된 채팅방 아이디 입니다.");

    private final HttpStatus status;
    private final String message;
}
