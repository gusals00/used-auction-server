package com.auction.usedauction.util;


import com.auction.usedauction.exception.CustomException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.auction.usedauction.exception.error_code.FileErrorCode.*;
import static com.auction.usedauction.util.FileSubPath.PRODUCT_IMG_PATH;
import static com.auction.usedauction.util.FileSubPath.STREAMING_VIDEO_PATH;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class S3FileUploaderTest {

    @Autowired
    private S3FileUploader fileUploader;

    @Test
    @DisplayName("S3에 multipart 파일 저장 성공")
    void fileUpload() throws Exception {
        //given
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String fileName4 = "test4.png";
        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        MockMultipartFile file4 = new MockMultipartFile("testFile4", fileName4, contentType, "test4".getBytes());

        List<MultipartFile> fileList = new ArrayList<>();
        fileList.add(file2);
        fileList.add(file3);
        fileList.add(file4);

        //when
        UploadFIleDTO uploadFileDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);
        List<UploadFIleDTO> uploadFileDTOS = fileUploader.uploadFiles(fileList, STREAMING_VIDEO_PATH);

        //then
        assertThat(uploadFileDTO.getStoreFullUrl()).contains(PRODUCT_IMG_PATH);
        assertThat(uploadFileDTO.getUploadFileName()).isEqualTo(fileName1);

        assertThat(uploadFileDTOS).extracting(UploadFIleDTO::getUploadFileName).containsExactly(fileName2, fileName3, fileName4);
    }

    @Test
    @DisplayName("S3에 multipart 파일 저장 실패, 비어있는 파일 전송시")
    void fileUploadFail() throws Exception {
        //given
        String fileName1 = "test1.png";
        String fileName3 = "test3.png";
        String fileName4 = "test4.png";
        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, (byte[]) null);
        MockMultipartFile file2 = null;
        MockMultipartFile file3 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        MockMultipartFile file4 = new MockMultipartFile("testFile4", fileName4, contentType, (byte[]) null);

        List<MultipartFile> fileList = new ArrayList<>();
        fileList.add(file3);
        fileList.add(file4);

        //then
        assertThatThrownBy(() -> fileUploader.uploadFile(file1, PRODUCT_IMG_PATH))
                .isInstanceOf(CustomException.class)
                .hasMessage(FILE_EMPTY.getMessage());

        assertThatThrownBy(() -> fileUploader.uploadFile(file2, PRODUCT_IMG_PATH))
                .isInstanceOf(CustomException.class)
                .hasMessage(FILE_EMPTY.getMessage());

        assertThatThrownBy(() -> fileUploader.uploadFiles(fileList, PRODUCT_IMG_PATH))
                .isInstanceOf(CustomException.class)
                .hasMessage(FILE_EMPTY.getMessage());

    }

    @Test
    @DisplayName("S3에 타입이 File인 파일 저장 성공")
    void fileUpload2() throws Exception {
        //given
        String fileName = "test1.txt";
        String path="src/test/resources/files/";
        File file = new File(path+fileName);

        //when
        UploadFIleDTO uploadFileDTO = fileUploader.uploadFile(file, STREAMING_VIDEO_PATH);

        //then
        assertThat(uploadFileDTO.getUploadFileName()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("S3에 타입이 File인 파일 저장 실패, 잘못된 경로, null인 파일 전송시")
    void fileUploadFail2() throws Exception {
        //given
        String fileName = "test2.txt";
        String path="src/test/resources/files/";
        File file = new File(path+fileName);

        //then
        assertThatThrownBy(() -> fileUploader.uploadFile(file, STREAMING_VIDEO_PATH))
                .isInstanceOf(CustomException.class)
                .hasMessage(FILE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("S3에서 파일 삭제 성공")
    void s3FileDeleteSuccess() throws Exception {
        //given
        String fileName1 = "test1.png";
        String contentType = "image/png";

        String fileName = "test1.txt";
        String path="src/test/resources/files/";

        //multipart 파일
        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        UploadFIleDTO uploadFileDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);

        //File
        File file = new File(path+fileName);
        UploadFIleDTO uploadFileDTO2 = fileUploader.uploadFile(file, STREAMING_VIDEO_PATH);

        //when
        String deletedPath1 = fileUploader.deleteFile(uploadFileDTO.getStoreUrl());
        String deletePath2 = fileUploader.deleteFile(uploadFileDTO2.getStoreUrl());

        //then
        assertThat(deletedPath1).isEqualTo(uploadFileDTO.getStoreUrl());
        assertThat(deletePath2).isEqualTo(uploadFileDTO2.getStoreUrl());
    }

    @Test
    @DisplayName("S3에서 파일 삭제 실패, 이미 삭제된 파일 삭제하려는 경우")
    void s3FileDeleteFail1() throws Exception {
        //given
        String fileName1 = "test1.png";
        String contentType = "image/png";

        String fileName = "test1.txt";
        String path="src/test/resources/files/";

        //multipart 파일
        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        UploadFIleDTO uploadFileDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);

        //File
        File file = new File(path+fileName);
        UploadFIleDTO uploadFileDTO2 = fileUploader.uploadFile(file, STREAMING_VIDEO_PATH);

        //when
        fileUploader.deleteFile(uploadFileDTO.getStoreUrl());
        fileUploader.deleteFile(uploadFileDTO2.getStoreUrl());

        //then
        assertThatThrownBy(() -> fileUploader.deleteFile(uploadFileDTO.getStoreUrl()))
                .isInstanceOf(CustomException.class)
                .hasMessage(S3_FILE_NOT_FOUND.getMessage());

        //then
        Assertions.assertThatThrownBy(() -> fileUploader.deleteFile(uploadFileDTO2.getStoreUrl()))
                .isInstanceOf(CustomException.class)
                .hasMessage(S3_FILE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("S3에서 파일 삭제 실패, 다른 경로의 파일 삭제하려는 경우")
    void s3FileDeleteFail2() throws Exception {
        //given
        String fileName1 = "test1.png";
        String contentType = "image/png";

        String fileName = "test1.txt";
        String path="src/test/resources/files/";

        //multipart 파일
        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        UploadFIleDTO uploadFileDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);

        //File
        File file = new File(path+fileName);
        UploadFIleDTO uploadFileDTO2 = fileUploader.uploadFile(file, STREAMING_VIDEO_PATH);

        //then
        assertThatThrownBy(() -> fileUploader.deleteFile(STREAMING_VIDEO_PATH + uploadFileDTO.getStoreFileName()))
                .isInstanceOf(CustomException.class)
                .hasMessage(S3_FILE_NOT_FOUND.getMessage());

        Assertions.assertThatThrownBy(() -> fileUploader.deleteFile(PRODUCT_IMG_PATH + uploadFileDTO2.getStoreFileName()))
                .isInstanceOf(CustomException.class)
                .hasMessage(S3_FILE_NOT_FOUND.getMessage());

    }
    
}