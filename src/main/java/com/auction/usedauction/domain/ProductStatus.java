package com.auction.usedauction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    DELETED("삭제"), // 삭제
    EXIST("존재"); // 존재

    private final String description;
}
