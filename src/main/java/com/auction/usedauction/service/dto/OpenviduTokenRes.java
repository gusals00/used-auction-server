package com.auction.usedauction.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenviduTokenRes {

    @Schema(example = "wss://openvidu.shop?sessionId=ses_FL20dvk&token=tok_4yYraijPA", description = "방송 토큰값")
    private String token;

    @Schema(example = "ses_FL20dvk", description = "세션 아이디")
    private String sessionId;
}
