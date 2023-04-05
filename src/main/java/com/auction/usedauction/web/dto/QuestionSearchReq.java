package com.auction.usedauction.web.dto;

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

    @NotNull
    @Min(0)
    private Integer page;
    @NotNull
    @Min(1)
    private Integer size;


}
