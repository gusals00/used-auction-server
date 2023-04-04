package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByIdAndProduct(Long id, Product product);
}
