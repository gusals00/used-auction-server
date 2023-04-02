package com.auction.usedauction.web.dto;

import com.auction.usedauction.repository.dto.ProductOrderCond;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchCondReq {
    @NotNull
    private Long categoryId;
    private String productName;
    @NotNull
    private ProductOrderCond orderBy;
    @NotNull
    private Integer page;
    @NotNull
    private Integer size;

}
