package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByIdAndProduct(Long id, Product product);
    Page<Question> findByProduct_IdAndParentIsNull(@Param("product_id")Long productId, Pageable pageable);
}
