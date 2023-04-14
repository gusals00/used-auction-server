package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.web.dto.CategoryRes;
import com.auction.usedauction.web.dto.ResultRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResultRes<List<CategoryRes>> getCategoryList() {
        return new ResultRes<>(categoryRepository.findAll().stream()
                .map(CategoryRes::new)
                .collect(toList()));
    }
}
