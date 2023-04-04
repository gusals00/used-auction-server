package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateReq {

    @NotEmpty
    @Schema(description = "이름", example = "김현민")
    private String name;

    @NotEmpty
    @Schema(description = "생년월일", example = "990828")
    private String birth;

    @NotBlank
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
}
