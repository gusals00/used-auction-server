package com.auction.usedauction.service;

import com.auction.usedauction.domain.Category;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.MemberStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.file.ProductImage;
import com.auction.usedauction.domain.file.ProductImageType;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.util.UploadFIleDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional

    public Long register(ProductRegisterDTO productRegisterDTO) {

        // 판매자 존재 체크
        Member member = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(productRegisterDTO.getLoginId(), MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 카테고리 존재 체크
        Category category = categoryRepository.findById(productRegisterDTO.getCategoryId())
                .orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //대표 사진 생성
        ProductImage productSigImage = createProductImage(productRegisterDTO.getSigProductImg(), ProductImageType.SIGNATURE);
        //일반 사진 생성
        List<ProductImage> productOrdinalImageList = createProductImageList(productRegisterDTO.getOrdinalProductImg(), ProductImageType.ORDINAL);

        //상품 저장
        Product product = createProduct(productRegisterDTO, productSigImage, productOrdinalImageList, member, category);
        productRepository.save(product);

        log.info("상품 등록 성공 productId={},sellerId={}, sigImgId={}, ordinalImgIds={}",
                product.getId(), member.getId(), productSigImage.getId(), getOrdinalImagesIdsToString(productOrdinalImageList));
        return product.getId();
    }

    public String getOrdinalImagesIdsToString(List<ProductImage> images) {
        return images.stream()
                .map(image -> String.valueOf(image.getId()))
                .toList()
                .toString();

    }

    private Product createProduct(ProductRegisterDTO productRegisterDTO, ProductImage productSigImage, List<ProductImage> productOrdinalImageList,
                                  Member member, Category category) {
        return Product.builder()
                .auctionEndDate(productRegisterDTO.getAuctionEndDate())
                .buyNowPrice(productRegisterDTO.getBuyNowPrice())
                .nowPrice(productRegisterDTO.getStartPrice())
                .priceUnit(productRegisterDTO.getPriceUnit())
                .startPrice(productRegisterDTO.getStartPrice())
                .info(productRegisterDTO.getInfo())
                .sigImage(productSigImage)
                .ordinalImageList(productOrdinalImageList)
                .category(category)
                .member(member)
                .name(productRegisterDTO.getName())
                .build();
    }

    private List<ProductImage> createProductImageList(List<UploadFIleDTO> uploadImageList, ProductImageType imageType) {
        return uploadImageList.stream()
                .map(uploadFIleDTO -> createProductImage(uploadFIleDTO, imageType))
                .collect(toList());
    }

    private ProductImage createProductImage(UploadFIleDTO uploadImage, ProductImageType imageType) {
        return ProductImage.builder()
                .originalName(uploadImage.getUploadFileName())
                .path(uploadImage.getStoreUrl())
                .fullPath(uploadImage.getStoreFullUrl())
                .type(imageType)
                .build();

    }
}
