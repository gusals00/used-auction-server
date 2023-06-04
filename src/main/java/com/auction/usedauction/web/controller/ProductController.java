package com.auction.usedauction.web.controller;


import com.auction.usedauction.aop.S3Rollback;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.BindingErrorCode;
import com.auction.usedauction.exception.error_code.FileErrorCode;
import com.auction.usedauction.repository.dto.ProductOrderCond;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.dto.*;
import com.auction.usedauction.service.query.ProductQueryService;
import com.auction.usedauction.util.s3.FileSubPath;
import com.auction.usedauction.util.s3.S3FileUploader;
import com.auction.usedauction.util.s3.UploadFileDTO;
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

import java.time.LocalDateTime;
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
    private final ProductRepository productRepository;

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

        return productQueryService.getProductPage(searchCond, pageRequest);
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

    @Operation(summary = "상품 등록 메서드")
    @PostMapping
    @S3Rollback
    public ResultRes<MessageRes> registerProduct(@Valid ProductRegisterReq registerReq, @AuthenticationPrincipal User user) {

        log.info("상품 등록 컨트롤러 호출");

        // 대표 사진 s3에 저장
        UploadFileDTO uploadSigFileDTO = fileUploader.uploadFile(registerReq.getSigImg(), FileSubPath.PRODUCT_IMG_PATH);

        // 일반 사진 s3에 저장
        List<UploadFileDTO> uploadOrdinalFileDTOS = uploadOrdinalImages(registerReq);

        // 현재 시간 기준 2일 뒤부터 경매 종료 날짜 가능
        LocalDateTime minPossibleEndTime = LocalDateTime.now().plusDays(2);
        if (!registerReq.getAuctionEndDate().isAfter(minPossibleEndTime)) {
            throw new CustomException(BindingErrorCode.POSSIBLE_REGISTER_END_TIME);
        }

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, uploadSigFileDTO, uploadOrdinalFileDTOS, user.getUsername());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        productService.register(productRegisterDTO, auctionRegisterDTO);
        return new ResultRes<>(new MessageRes("상품 등록 성공"));
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

        productService.deleteProduct(productId, user.getUsername());
        return new ResultRes<>(new MessageRes("상품 삭제를 성공했습니다."));
    }

    @Operation(summary = "상품 수정 메서드")
    @PatchMapping("/update/{productId}")
    public ResultRes<MessageRes> updateProduct(@PathVariable Long productId, @Valid ProductUpdateReq updateReq, @AuthenticationPrincipal User user) {
        log.info("상품 수정 컨트롤러 호출");

        //일반 사진들, 대표 사진이 비어있는지 확인
        if (isEmptyMultipartFileList(updateReq.getImgList()) || isEmptyMultipartFile(updateReq.getSigImg())) {
            throw new CustomException(FileErrorCode.FILE_EMPTY);
        }

        // 현재 시간 기준 4시간 이후부터 경매 종료 날짜로 수정 가능
        LocalDateTime minPossibleEndTime = LocalDateTime.now().plusHours(4);
        if (!updateReq.getAuctionEndDate().isAfter(minPossibleEndTime)) {
            throw new CustomException(BindingErrorCode.POSSIBLE_UPDATE_END_TIME);
        }

        productService.updateProduct(productId, updateReq, user.getUsername());

        return new ResultRes<>(new MessageRes("상품 수정을 성공했습니다."));
    }

    @Operation(summary = "상품 수정 정보 조회 메서드")
    @GetMapping("/update/{productId}")
    public ResultRes<ProductUpdateInfoRes> updateProduct(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        log.info("상품 수정 정보 조회 컨트롤러 호출");
        return new ResultRes<>(productQueryService.getProductUpdateInfo(productId, user.getUsername()));
    }

    @Operation(summary = "판매자가 판매하는 상품인지 확인")
    @GetMapping("/valid/{productId}")
    public ResultRes<ValidResult> validRightSellerForProduct(@PathVariable Long productId,@AuthenticationPrincipal User user) {
        log.info("validRightSellerForProduct controller");
        return new ResultRes<>(new ValidResult(productRepository.existProductByIdAndLoginId(productId, user.getUsername())));
    }

    private List<UploadFileDTO> uploadOrdinalImages(ProductRegisterReq registerReq) {

        List<UploadFileDTO> uploadOrdinalFileDTOS;

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

    private boolean isEmptyMultipartFileList(List<MultipartFile> fileList) {
        if (fileList == null) {
            return true;
        }

        for (MultipartFile file : fileList) {
            if (isEmptyMultipartFile(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmptyMultipartFile(MultipartFile file) {
        return (file == null) || (file.isEmpty());
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
            this.description = orderCond.getDescription();
        }
    }
}
