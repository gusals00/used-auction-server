package com.auction.usedauction.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadFIleDTO {
    private String uploadFileName; // 업로드 시 파일 이름
    private String storeFileName; // S3에 저장 시 파일 이름
    private String storeUrl; // S3에 저장 시 URL

    private String storeFullUrl; // S3에 저장 시 full URL
}
