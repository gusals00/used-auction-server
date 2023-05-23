package com.auction.usedauction.exception.error_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ErrorCode {

    S3_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST,"S3에서 해당 파일을 찾지 못했습니다."),
    S3_FILE_NOT_TRANSFER(HttpStatus.BAD_REQUEST,"S3에서 해당 파일을 전송하지 못했습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST,"파일이 비어 있습니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST,"해당 경로에 파일이 없습니다."),
    NO_FILE_NAME(HttpStatus.BAD_REQUEST,"파일 이름이 비어 있습니다.");
    private final HttpStatus status;
    private final String message;
}
