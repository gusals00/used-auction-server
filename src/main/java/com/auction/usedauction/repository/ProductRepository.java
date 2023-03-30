package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
