package com.auction.usedauction.service.query;


import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.*;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.PageListRes;
import com.auction.usedauction.service.dto.ProductDetailInfoRes;
import com.auction.usedauction.service.dto.ProductPageContentRes;
import com.auction.usedauction.service.dto.ProductUpdateInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auction.usedauction.domain.ProductStatus.EXIST;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProductQueryService {
    private final ProductRepository productRepository;

    //상품 리스트 조회
    public PageListRes<ProductPageContentRes> getProductPage(ProductSearchCondDTO searchCond, Pageable pageable) {

        Page<Product> findPage = productRepository.findBySearchCond(searchCond, pageable);
        List<Product> contents = findPage.getContent();

        //라이브 중인지는 나중에 추가할 예정
        List<ProductPageContentRes> productListContents = contents.stream()
                .map(ProductPageContentRes::new)
                .toList();
        return new PageListRes<>(productListContents, findPage);
    }

    @Transactional
    public ProductDetailInfoRes getProductDetail(Long productId) {
        Product findProduct = productRepository.findExistProductByIdAndExistMember(productId)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 조회수 증가
        findProduct.increaseViewCount();

        //라이브 중인지는 나중에 추가할 예정

        return new ProductDetailInfoRes(findProduct);
    }

    public ProductUpdateInfoRes getProductUpdateInfo(Long productId, String loginId) {
        //상품이 존재하는지 확인
        Product findProduct = productRepository.findByIdAndProductStatus(productId, EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        //수정하려는 판매자가 올바른 판매자인지(상품 등록자가 맞는지 + 상품 등록자 상태가 EXIST 인지)
        validRightSeller(findProduct, loginId);
        return new ProductUpdateInfoRes(findProduct);
    }

    private void validRightSeller(Product product, String loginId) {
        if (!isRightSeller(product, loginId)) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
    }

    private boolean isRightSeller(Product product, String loginId) {
        return product.getMember().getLoginId().equals(loginId) && product.getMember().getStatus() == MemberStatus.EXIST;
    }
}
