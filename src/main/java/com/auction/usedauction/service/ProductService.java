package com.auction.usedauction.service;

import com.auction.usedauction.UploadFIleDTO;
import com.auction.usedauction.repository.ProductRepository;
import com.auction.usedauction.service.dto.ProductRegisterReq;
import com.auction.usedauction.util.S3FileUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.auction.usedauction.util.FileSubPath.PRODUCT_IMG_PATH;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final S3FileUploader fileUploader;

    public Long register(ProductRegisterReq productRegisterReq) throws IOException {
        // 판매자 존재 체크

        // 카테고리 존재 체크

        //대표 사진 생성
        MultipartFile sigImg = productRegisterReq.getSigImg();
        UploadFIleDTO sigImgUploadFileDTO = fileUploader.uploadFile(sigImg, PRODUCT_IMG_PATH);

        //일반 사진 생성(없으면 대표 사진 넣기)

        //상품 저장
        return 1L;
    }
}
