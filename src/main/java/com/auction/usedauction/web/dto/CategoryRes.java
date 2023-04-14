package com.auction.usedauction.web.dto;

import com.auction.usedauction.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRes {

    private Long categoryId;
    private String name;

    public CategoryRes(Category category) {
        this.categoryId = category.getId();
        this.name = category.getName();
    }
}
