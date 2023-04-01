package com.auction.usedauction.domain.file;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImage extends File {

    @Enumerated(EnumType.STRING)
    private ProductImageType type;

    @Builder
    public ProductImage(String fullPath, String path, String originalName, ProductImageType type) {
        super(fullPath, path, originalName);
        this.type = type;
    }
}
