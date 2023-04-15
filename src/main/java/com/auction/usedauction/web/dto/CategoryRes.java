package com.auction.usedauction.web.dto;

import com.auction.usedauction.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRes {

    @Schema(description = "카테고리 ID",example = "1")
    private Long categoryId;
    @Schema(description = "카테고리 이름",example = "스포츠/레저")
    private String name;

    public CategoryRes(Category category) {
        this.categoryId = category.getId();
        this.name = category.getName();
    }
}
