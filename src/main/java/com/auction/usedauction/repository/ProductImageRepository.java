package com.auction.usedauction.repository;

import com.auction.usedauction.domain.file.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
}