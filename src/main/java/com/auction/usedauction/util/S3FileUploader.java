package com.auction.usedauction.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.auction.usedauction.UploadFIleDTO;
import com.auction.usedauction.exception.FileEmptyException;
import com.auction.usedauction.exception.S3FileNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3FileUploader {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<UploadFIleDTO> uploadFiles(List<MultipartFile> multipartFileList, String subPath) throws IOException {
        if (multipartFileList == null) {
            throw new FileEmptyException("파일이 비어 있습니다.");
        }
        List<UploadFIleDTO> storeResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFileList) {
            storeResult.add(uploadFile(multipartFile, subPath));
        }

        return storeResult;
    }

    public UploadFIleDTO uploadFile(MultipartFile multipartFile, String subPath) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileEmptyException("파일이 비어 있습니다.");
        }

        String originalFileName = getOriginalFileName(multipartFile);
        String storeFileName = createStoreFileName(originalFileName);

        String storeFileUrl = sendAwsS3(bucket, subPath + storeFileName, multipartFile);
        log.info("S3에 파일 전송 완료 originalFileName = {},fileSubPath = {}, storeUrl={}", originalFileName, subPath, storeFileUrl);

        return new UploadFIleDTO(originalFileName, storeFileName, storeFileUrl);
    }

    private String sendAwsS3(String bucketName, String filePath, MultipartFile uploadFile) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(uploadFile.getContentType());
        objectMetadata.setContentLength(uploadFile.getSize());

        amazonS3Client.putObject(new PutObjectRequest(bucketName, filePath, uploadFile.getInputStream(), objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucketName, filePath).toString();

    }

    public String deleteFile(String filePath) {
        boolean isExist = amazonS3Client.doesObjectExist(bucket, filePath);
        if (!isExist) {
            throw new S3FileNotFoundException("S3에서 해당 파일을 찾지 못했습니다.");
        }
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, filePath));
        log.info("S3 파일 삭제 완료 deletedFilePath = {}", filePath);
        return filePath;
    }

    public String getOriginalFileName(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileEmptyException("파일이 비어 있습니다.");
        }
        return multipartFile.getOriginalFilename();
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename); // 확장자 추출
        return UUID.randomUUID().toString() + "." + ext; // wfese-wfe-223.png
    }

    private static String extractExt(String originalFilename) { // 확장자 추출
        int position = originalFilename.lastIndexOf(".");
        return originalFilename.substring(position + 1);
    }
}
