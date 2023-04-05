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
public class QuestionSearchReq {

    @Schema(description = "페이저 번호, 0부터 시작",example = "0")
    @NotNull
    @Min(0)
    private Integer page;

    @Schema(description = "페이저 사이즈, 1 이상",example = "10")
    @NotNull
    @Min(1)
    private Integer size;


}
