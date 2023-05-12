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
public class IsLiveRes {

    @Schema(description = "라이브 여부 확인, true -> 방송중O/false -> 방송중X",example = "true")
    private boolean isLive;


}
