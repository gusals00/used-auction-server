package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.web.dto.CategoryRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "카테고리 컨트롤러", description = "카테고리 관련 api")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "카테고리 리스트 조회 메서드")
    public ResultRes<List<CategoryRes>> getCategoryList() {
        return new ResultRes<>(categoryRepository.findAll().stream()
                .map(CategoryRes::new)
                .collect(toList()));
    }
}
