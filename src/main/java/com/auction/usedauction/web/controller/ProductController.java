package com.auction.usedauction.web.controller;


import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.dto.ProductPageRes;
import com.auction.usedauction.service.dto.ProductPageContentRes;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.service.query.ProductQueryService;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFIleDTO;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "상품 컨트롤러", description = "상품 관련 api")
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService;
    private final S3FileUploader fileUploader;

    @Operation(summary = "상품 리스트 조회 메서드",parameters = {})
    @GetMapping
    public PageListRes<ProductPageContentRes> getProductList(@ParameterObject @Valid ProductSearchCondReq searchCondReq) {
        log.info("상품 리스트 조히 컨트롤러 호출");

        log.info("검색 조건 - 카테고리={}, 상품이름 = {}, 정렬 = {}, 현재 페이지 번호 = {}, 페이지 사이즈 = {}",
                searchCondReq.getCategoryId(),searchCondReq.getProductName(),searchCondReq.getOrderBy(),searchCondReq.getPage(),searchCondReq.getSize());

        PageRequest pageRequest = PageRequest.of(searchCondReq.getPage(), searchCondReq.getSize());
        ProductSearchCondDTO searchCond = createProductSearchCond(searchCondReq);

        ProductPageRes productPage = productQueryService.getProductPage(searchCond, pageRequest);

        return new PageListRes(productPage.getProductPageContents(),productPage.getPage());
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

        List<UploadFIleDTO> uploadOrdinalFileDTOS = null;

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
}
