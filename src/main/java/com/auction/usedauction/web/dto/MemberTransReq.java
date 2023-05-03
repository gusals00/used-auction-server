package com.auction.usedauction.web.dto;

import com.auction.usedauction.domain.TransStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberTransReq {

    @Schema(description = "경매 ID",example = "1")
    @NotNull
    private Long auctionId;

    @Schema(description = "거래 상태, 거래 확정시 거래 상태 기준 참고",example = "TRANS_COMPLETE")
    @NotNull
    private TransStatus status;
}
