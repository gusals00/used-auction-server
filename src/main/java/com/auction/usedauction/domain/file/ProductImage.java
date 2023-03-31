package com.auction.usedauction.domain.file;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.file.File;
import com.auction.usedauction.domain.file.ProductImageType;
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
    public ProductImage(String fullPath, String path, String originalName, ProductImageType type) {
        super(fullPath, path, originalName);
        this.type = type;
    }
}
