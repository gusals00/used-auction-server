package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode{

    NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST,"해당 알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
