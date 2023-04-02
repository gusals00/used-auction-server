package com.auction.usedauction.service.query;


import com.auction.usedauction.domain.Product;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.ProductPageRes;
import com.auction.usedauction.service.dto.ProductPageContentRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProductQueryService {
    private final ProductRepository productRepository;

    //상품 리스트 조회
    public ProductPageRes getProductPage(ProductSearchCondDTO searchCond, Pageable pageable) {
        Page<Product> findPage = productRepository.findBySearchCond(searchCond, pageable);
        List<Product> contents = findPage.getContent();

        //라이브 중인지는 나중에 추가할 예정
        List<ProductPageContentRes> productListContents = contents.stream()
                .map(ProductPageContentRes::new)
                .toList();

        return new ProductPageRes(productListContents,findPage);
    }
}
