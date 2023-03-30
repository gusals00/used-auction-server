package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findCategoryByName(String name);
}
