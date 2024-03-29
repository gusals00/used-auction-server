package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenviduRemoveUserReq {

    @Schema(example = "11", description = "상품 아이디")
    private Long productId;

    @Schema(example = "wss://openvidu.shop?sessionId=ses_FL20dvk&token=tok_4yYraijPA", description = "방송 토큰값")
    private String token;
}
