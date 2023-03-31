package com.auction.usedauction.web.controller;


import com.auction.usedauction.repository.ProductImageRepository;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFIleDTO;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import com.auction.usedauction.web.dto.ResultRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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
public class ProductController {

    private final ProductService productService;
    private final S3FileUploader fileUploader;

    @PostMapping
    public ResultRes registerProduct(@Valid ProductRegisterReq registerReq, @AuthenticationPrincipal User user) {

        // 대표 사진 s3에 저장
        UploadFIleDTO uploadSigFileDTO = fileUploader.uploadFile(registerReq.getSigImg(), FileSubPath.PRODUCT_IMG_PATH);

        // 일반 사진 s3에 저장
        List<UploadFIleDTO> uploadOrdinalFileDTOS = uploadOrdinalImages(registerReq);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, uploadSigFileDTO, uploadOrdinalFileDTOS, user.getUsername());

        productService.register(productRegisterDTO);
        return new ResultRes<>(new MessageRes("상품 등록 성공"));
    }

    private List<UploadFIleDTO> uploadOrdinalImages(ProductRegisterReq registerReq) {

        // 일반 사진들이 없으면 대표사진을 일반 사진에 저장

        List<UploadFIleDTO> uploadOrdinalFileDTOS = null;

        if (registerReq.getImgList() == null) {
            List<MultipartFile> imgList = new ArrayList<>();
            imgList.add(registerReq.getSigImg());
            uploadOrdinalFileDTOS = fileUploader.uploadFiles(imgList, FileSubPath.PRODUCT_IMG_PATH);
        } else {
            uploadOrdinalFileDTOS = fileUploader.uploadFiles(registerReq.getImgList(), FileSubPath.PRODUCT_IMG_PATH);
        }
        return uploadOrdinalFileDTOS;
    }
}
