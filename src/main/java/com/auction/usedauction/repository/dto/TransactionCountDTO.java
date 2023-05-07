package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionCountDTO {

    @Schema(description = "전체 판매 횟수",example = "2")
    private Long allCount;

    @Schema(description = "성공 판매 횟수",example = "1")
    private Long successCount;

    @QueryProjection
    public TransactionCountDTO(Long allCount, Long successCount) {
        this.allCount = allCount;
        this.successCount = successCount;
    }
}
