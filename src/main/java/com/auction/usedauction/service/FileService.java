package com.auction.usedauction.service;

import com.auction.usedauction.aop.S3Rollback;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import com.auction.usedauction.domain.file.ProductVideo;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.FileErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.file.FileRepository;
import com.auction.usedauction.repository.file.ProductVideoRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {
    private final FileRepository fileRepository;
    private final ProductVideoRepository productVideoRepository;
    private final S3FileUploader fileUploader;
    private final ProductRepository productRepository;

    @Transactional
    @S3Rollback
    public Long registerVideoFile(Long productId, File file) {
        UploadFileDTO uploadFileDTO = fileUploader.uploadFile(file, FileSubPath.STREAMING_VIDEO_PATH);
        Product findProduct = productRepository.findByIdAndProductStatus(productId, ProductStatus.EXIST).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        return fileRepository.save(createProductVideo(uploadFileDTO, findProduct)).getId();
    }

    @Transactional
    @S3Rollback
    public Long deleteVideo(Long videoId,String loginId) {

        ProductVideo productVideo = productVideoRepository.findByProductVideoFetch(videoId)
                .orElseThrow(() -> new CustomException(FileErrorCode.FILE_NOT_FOUND));
        // 판매자가 아닌 경우
        if (!productVideo.getProduct().getMember().getLoginId().equals(loginId)) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
        productVideoRepository.deleteById(productVideo.getId());
        fileUploader.deleteFile(productVideo.getPath());
        return productVideo.getId();
    }

    private ProductVideo createProductVideo(UploadFileDTO uploadFileDTO, Product product) {
        ProductVideo productVideo = ProductVideo.builder()
                .originalName(uploadFileDTO.getUploadFileName())
                .path(uploadFileDTO.getStoreUrl())
                .fullPath(uploadFileDTO.getStoreFullUrl())
                .build();
        productVideo.changeProduct(product);
        return productVideo;
    }
}
