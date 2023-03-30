package com.auction.usedauction.web.controller;

import com.auction.usedauction.domain.Member;

import com.auction.usedauction.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final CategoryService categoryService;

    @PostMapping
    public Long registerProduct(List<MultipartFile> file) throws IOException {
        System.out.println(file.size());
        System.out.println(file.get(0));
        System.out.println(file.get(0).isEmpty());
        return 1L;

    }

    @GetMapping
    public List<Member> test() {
        log.info("querydsl");
        List<Member> test = categoryService.test();
        log.info("size={}",test.size());
        System.out.println(test);
        return test;
    }
}
