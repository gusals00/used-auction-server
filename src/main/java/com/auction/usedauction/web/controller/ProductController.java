package com.auction.usedauction.web.controller;


import com.auction.usedauction.repository.dto.ProductOrderCond;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.dto.*;
import com.auction.usedauction.service.query.ProductQueryService;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFIleDTO;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;


@RestController
@Slf4j
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "상품 컨트롤러", description = "상품 관련 api")
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService;
    private final S3FileUploader fileUploader;

    @Operation(summary = "상품 리스트 조회 메서드")
    @GetMapping
    public PageListRes<ProductPageContentRes> getProductList(@ParameterObject @Valid ProductSearchCondReq searchCondReq) {
        log.info("상품 리스트 조회 컨트롤러 호출");

        log.info("검색 조건 - 카테고리={}, 상품이름 = {}, 정렬 = {}, 현재 페이지 번호 = {}, 페이지 사이즈 = {}",
                searchCondReq.getCategoryId(), searchCondReq.getProductName(), searchCondReq.getOrderBy(), searchCondReq.getPage(), searchCondReq.getSize());

        PageRequest pageRequest = PageRequest.of(searchCondReq.getPage(), searchCondReq.getSize());
        ProductSearchCondDTO searchCond = createProductSearchCond(searchCondReq);

        ProductPageRes productPage = productQueryService.getProductPage(searchCond, pageRequest);

        return new PageListRes<>(productPage.getProductPageContents(), productPage.getPage());
    }

    @Operation(summary = "상품 리스트 조회 시 정렬 기준")
    @GetMapping("/order-by")
    public ResultRes<List<OrderByRes>> getProductOderByList() {
        log.info("상품 리스트 조회시 정렬 기준");

        return new ResultRes<>(Arrays
                .stream(ProductOrderCond.values())
                .map(OrderByRes::new)
                .collect(toList()));
    }

    @Getter
    @Setter
    static class OrderByRes {
        @Schema(description = "정렬 기준(orderBy)", example = "VIEW_ORDER")
        private String name;
        @Schema(description = "설명", example = "조회순")
        private String description;

        public OrderByRes(ProductOrderCond orderCond) {
            this.name = orderCond.name();
            this.description = orderCond.getDescrpition();
        }
    }

    @Operation(summary = "상품 등록 메서드")
    @PostMapping
    public ResultRes<MessageRes> registerProduct(@Valid ProductRegisterReq registerReq, @AuthenticationPrincipal User user) {

        log.info("상품 등록 컨트롤러 호출");

        // 대표 사진 s3에 저장
        UploadFIleDTO uploadSigFileDTO = fileUploader.uploadFile(registerReq.getSigImg(), FileSubPath.PRODUCT_IMG_PATH);

        // 일반 사진 s3에 저장
        List<UploadFIleDTO> uploadOrdinalFileDTOS = uploadOrdinalImages(registerReq);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, uploadSigFileDTO, uploadOrdinalFileDTOS, user.getUsername());
        productService.register(productRegisterDTO);
        return new ResultRes<>(new MessageRes("상품 등록 성공"));
    }

    private List<UploadFIleDTO> uploadOrdinalImages(ProductRegisterReq registerReq) {

        List<UploadFIleDTO> uploadOrdinalFileDTOS;

        if (isEmptyImgList(registerReq.getImgList())) {// 일반 사진들이 없으면 대표사진을 일반 사진에 저장
            List<MultipartFile> imgList = new ArrayList<>();
            imgList.add(registerReq.getSigImg());
            uploadOrdinalFileDTOS = fileUploader.uploadFiles(imgList, FileSubPath.PRODUCT_IMG_PATH);
        } else { // 일반 사진이 있는 경우
            uploadOrdinalFileDTOS = fileUploader.uploadFiles(registerReq.getImgList(), FileSubPath.PRODUCT_IMG_PATH);
        }

        return uploadOrdinalFileDTOS;
    }

    private boolean isEmptyImgList(List<MultipartFile> multipartFileList) {
        if (multipartFileList == null) { // 리스트가 null인 경우
            return true;
        }
        for (MultipartFile multipartFile : multipartFileList) {
            if (fileUploader.isEmptyFile(multipartFile)) { // multipartFile이 empty인 경우
                return true;
            }
        }

        return false;
    }

    private ProductSearchCondDTO createProductSearchCond(ProductSearchCondReq searchCondReq) {
        Long categoryId = searchCondReq.getCategoryId() == 0 ? null : searchCondReq.getCategoryId();
        return new ProductSearchCondDTO(categoryId, searchCondReq.getProductName(), searchCondReq.getOrderBy());
    }

    @Operation(summary = "상품 상세 조회 메서드")
    @GetMapping("/{productId}")
    public ResultRes<ProductDetailInfoRes> getProduct(@PathVariable Long productId) {
        log.info("상품 상세 조회 컨트롤러 호출");
        log.info("찾는 productId = {}", productId);


        return new ResultRes<>(productQueryService.getProductDetail(productId));
    }

    @Operation(summary = "상품 삭제 메서드")
    @DeleteMapping("/{productId}")
    public ResultRes<MessageRes> deleteProduct(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        log.info("상품 삭제 컨트롤러 호출");
        log.info("삭제하려는 productId = {}", productId);

        productService.deleteProduct(productId,user.getUsername());
        return new ResultRes<>(new MessageRes("상품 삭제를 성공했습니다."));
    }

//    @PatchMapping("/{productId}")
//    public ResultRes updateProduct(@PathVariable Long productId,@RequestBody @Valid ProductUpdateReq,@AuthenticationPrincipal User user) {
//
//    }

}
