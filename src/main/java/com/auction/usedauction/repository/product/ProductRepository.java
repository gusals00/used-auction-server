package com.auction.usedauction.repository.product;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @EntityGraph(attributePaths = "member")
    Optional<Product> findByIdAndProductStatusNot(Long id, ProductStatus productStatus);

    @EntityGraph(attributePaths = "member")
    Optional<Product> findByIdAndProductStatusIn(Long id, ProductStatus[] productStatus);

    @EntityGraph(attributePaths = "member")
    Optional<Product> findByIdAndProductStatus(Long id, ProductStatus productStatus);

}
