package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {

    @NotEmpty
    @Schema(description = "이름", example = "김현민")
    private String name;

    @NotBlank
    @Schema(description = "로그인 아이디", example = "hyeonmin")
    private String loginId;

    @NotBlank
    @Schema(description = "비밀번호", example = "password")
    private String password;

    @NotEmpty
    @Schema(description = "생년월일", example = "990828")
    private String birth;

    @Pattern(regexp = "^[\\w-\\.]+@kumoh.ac.kr$")
    @Schema(description = "이메일", example = "a12b12c@kumoh.ac.kr")
    private String email;

    @NotBlank
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @NotBlank
    @Schema(description = "이메일 인증 코드", example = "0Pjaz615")
    private String code;
}
