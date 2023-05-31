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

    private Long allCount;

    private Long successCount;

    @QueryProjection
    public TransactionCountDTO(Long allCount, Long successCount) {
        this.allCount = allCount;
        if(successCount == null) {
            this.successCount = 0L;
        } else {
            this.successCount = successCount;
        }
    }
}
