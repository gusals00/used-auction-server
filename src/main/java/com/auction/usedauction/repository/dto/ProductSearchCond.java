package com.auction.usedauction.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchCond {
    private Long categoryId;
    private String productName;
    private ProductOrderCond orderBy;
}
