package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionAndRejectCountRes {

    @Schema(description = "전체 판매 횟수",example = "2")
    private Long allCount;

    @Schema(description = "성공 판매 횟수",example = "1")
    private Long successCount;

    @Schema(description = "경고 횟수", example = "2")
    private Long rejectCount;
}
