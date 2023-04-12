package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageSearchConReq {

    @Schema(description = "상태별 조회, 없을시 전체조회", example = "BID", nullable = true)
    private String status;

    @NotNull
    @Min(0)
    @Schema(description = "페이지 번호 , 0부터 시작", example = "0", minimum = "0")
    private Integer page;

    @NotNull
    @Min(1)
    @Schema(description = "페이지 크기 ,1 이상", example = "10", minimum = "1")
    private Integer size;
}
