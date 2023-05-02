package com.auction.usedauction.exception.error_code;


import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.ErrorRes;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ErrorCodeTest {

    @Test
    @DisplayName("예외 메시지가 ErrorCode에 맞게 잘 저장되는지")
    void splitErrorCodeTest() throws Exception{
        //given
        CustomException fileNotFoundCustomException = new CustomException(FileErrorCode.FILE_NOT_FOUND);
        CustomException s3FileCustomException = new CustomException(FileErrorCode.S3_FILE_NOT_FOUND);
        CustomException userNotFoundCustomException = new CustomException(UserErrorCode.USER_NOT_FOUND);

        //then
        assertThatThrownBy(() -> { throw fileNotFoundCustomException; })
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(FileErrorCode.FILE_NOT_FOUND.getMessage());

        assertThatThrownBy(() -> { throw s3FileCustomException; })
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(FileErrorCode.S3_FILE_NOT_FOUND.getMessage());

        assertThatThrownBy(() -> { throw userNotFoundCustomException; })
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("ResultRes 생성시 ErrorCode에 맞는 code, message,HttpStatus 인지 확인")
    void ResultResTest() throws Exception{

        //given

        //ErrorCode
        FileErrorCode fileNotFound = FileErrorCode.FILE_NOT_FOUND;
        FileErrorCode s3FileNotFound = FileErrorCode.S3_FILE_NOT_FOUND;
        UserErrorCode userNotFound = UserErrorCode.USER_NOT_FOUND;
        //Exception
        CustomException fileNotFoundCustomException = new CustomException(fileNotFound);
        CustomException s3FileCustomException = new CustomException(s3FileNotFound);
        CustomException userNotFoundCustomException = new CustomException(userNotFound);

        //when
        ResponseEntity<ErrorRes> fileErrorResponseEntity = ErrorRes.error(fileNotFoundCustomException);
        ResponseEntity<ErrorRes> s3ErrorResponseEntity = ErrorRes.error(s3FileCustomException);
        ResponseEntity<ErrorRes> userrrorResponseEntity = ErrorRes.error(userNotFoundCustomException);

        //then
        // FileErrorCode.FILE_NOT_FOUND -> ResponseEntity 생성시
        Assertions.assertThat(fileErrorResponseEntity.getBody().getCode()).isEqualTo(fileNotFound.name());
        Assertions.assertThat(fileErrorResponseEntity.getBody().getStatus()).isEqualTo(fileNotFound.getStatus());
        Assertions.assertThat(fileErrorResponseEntity.getBody().getMsg()).isEqualTo(fileNotFound.getMessage());

        // FileErrorCode.S3_FILE_NOT_FOUND -> ResponseEntity 생성시
        Assertions.assertThat(s3ErrorResponseEntity.getBody().getCode()).isEqualTo(s3FileNotFound.name());
        Assertions.assertThat(s3ErrorResponseEntity.getBody().getStatus()).isEqualTo(s3FileNotFound.getStatus());
        Assertions.assertThat(s3ErrorResponseEntity.getBody().getMsg()).isEqualTo(s3FileNotFound.getMessage());

        // UserErrorCode.USER_NOT_FOUND -> ResponseEntity 생성시
        Assertions.assertThat(userrrorResponseEntity.getBody().getCode()).isEqualTo(userNotFound.name());
        Assertions.assertThat(userrrorResponseEntity.getBody().getStatus()).isEqualTo(userNotFound.getStatus());
        Assertions.assertThat(userrrorResponseEntity.getBody().getMsg()).isEqualTo(userNotFound.getMessage());

    }

}