package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImage extends File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductImageType type;

    @Builder
    public ProductImage(String fullPath, String path, String name, Product product, ProductImageType type) {
        super(fullPath, path, name, product);
        this.type = type;
    }
}
