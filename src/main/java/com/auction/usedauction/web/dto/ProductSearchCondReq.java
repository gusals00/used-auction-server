package com.auction.usedauction.web.dto;

import com.auction.usedauction.repository.dto.ProductOrderCond;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchCondReq {
    private Long categoryId;
    private String productName;
    private ProductOrderCond orderBy;

}
