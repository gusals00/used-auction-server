package com.auction.usedauction.util;


import com.auction.usedauction.UploadFIleDTO;
import com.auction.usedauction.exception.FileEmptyException;
import com.auction.usedauction.exception.S3FileNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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
        UploadFIleDTO uploadFIleDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);
        List<UploadFIleDTO> uploadFIleDTOS = fileUploader.uploadFiles(fileList, STREAMING_VIDEO_PATH);

        //then
        assertThat(uploadFIleDTO.getStoreFullUrl()).contains(PRODUCT_IMG_PATH);
        assertThat(uploadFIleDTO.getUploadFileName()).isEqualTo(fileName1);

        assertThat(uploadFIleDTOS).extracting(UploadFIleDTO::getUploadFileName).containsExactly(fileName2, fileName3, fileName4);
    }

    @Test
    @DisplayName("S3에 multipart 파일 저장 실패, 비어있는 파일 전송시")
    void fileUploadFail() throws Exception {
        //given
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
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
        Assertions.assertThatThrownBy(() -> fileUploader.uploadFile(file1, PRODUCT_IMG_PATH))
                .isInstanceOf(FileEmptyException.class)
                .hasMessage("파일이 비어 있습니다.");

        Assertions.assertThatThrownBy(() -> fileUploader.uploadFile(file2, PRODUCT_IMG_PATH))
                .isInstanceOf(FileEmptyException.class)
                .hasMessage("파일이 비어 있습니다.");

        Assertions.assertThatThrownBy(() -> fileUploader.uploadFiles(fileList, PRODUCT_IMG_PATH))
                .isInstanceOf(FileEmptyException.class)
                .hasMessage("파일이 비어 있습니다.");

    }

    @Test
    @DisplayName("S3에서 파일 삭제 성공")
    void s3FileDeleteSuccess() throws Exception {
        //given
        String fileName1 = "test1.png";
        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        UploadFIleDTO uploadFIleDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);

        //when
        String deletedPath = fileUploader.deleteFile(uploadFIleDTO.getStoreUrl());

        //then
        assertThat(deletedPath).isEqualTo(uploadFIleDTO.getStoreUrl());
    }

    @Test
    @DisplayName("S3에서 파일 삭제 실패, 이미 삭제된 파일 삭제하려는 경우")
    void s3FileDeleteFail1() throws Exception {
        //given
        String fileName1 = "test1.png";
        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        UploadFIleDTO uploadFIleDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);

        //when
        fileUploader.deleteFile(uploadFIleDTO.getStoreUrl());

        //then
        Assertions.assertThatThrownBy(() -> fileUploader.deleteFile(uploadFIleDTO.getStoreUrl()))
                .isInstanceOf(S3FileNotFoundException.class)
                .hasMessage("S3에서 해당 파일을 찾지 못했습니다.");
    }

    @Test
    @DisplayName("S3에서 파일 삭제 실패, 다른 경로의 파일 삭제하려는 경우")
    void s3FileDeleteFail2() throws Exception {
        //given
        String fileName1 = "test1.png";
        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        UploadFIleDTO uploadFIleDTO = fileUploader.uploadFile(file1, PRODUCT_IMG_PATH);

        //then
        Assertions.assertThatThrownBy(() -> fileUploader.deleteFile(STREAMING_VIDEO_PATH + uploadFIleDTO.getStoreFileName()))
                .isInstanceOf(S3FileNotFoundException.class)
                .hasMessage("S3에서 해당 파일을 찾지 못했습니다.");
    }
    
}