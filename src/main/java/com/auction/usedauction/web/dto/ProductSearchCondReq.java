package com.auction.usedauction.web.dto;

import com.auction.usedauction.repository.dto.ProductOrderCond;

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
public class ProductSearchCondReq {
    @NotNull
    @Schema(description = "카테고리 ID",example = "1",nullable = true)
    private Long categoryId;
    @Schema(description = "상품 이름",example = "자바 프로그래밍 팔아요",nullable = true)
    private String productName;
    @NotNull
    @Schema(description = "정렬 순서",example = "VIEW_ORDER")
    private ProductOrderCond orderBy;
    @NotNull
    @Min(0)
    @Schema(description = "페이지 번호 , 0부터 시작",example = "0",minimum = "0")
    private Integer page;
    @NotNull
    @Min(1)
    @Schema(description = "페이지 크기 ,1 이상",example = "10",minimum = "1")
    private Integer size;

}
