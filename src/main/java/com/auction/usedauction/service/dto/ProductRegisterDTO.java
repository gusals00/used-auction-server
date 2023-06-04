package com.auction.usedauction.service.dto;

import com.auction.usedauction.util.s3.UploadFileDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRegisterDTO {

    private String name;
    private String info;
    private Long categoryId;

    private UploadFileDTO sigProductImg;
    private List<UploadFileDTO> ordinalProductImg;
    private String loginId;

    public ProductRegisterDTO(ProductRegisterReq registerReq, UploadFileDTO sigProductImg, List<UploadFileDTO> ordinalProductImg, String loginId) {
        this.name = registerReq.getName();
        this.info = registerReq.getInfo();
        this.categoryId = registerReq.getCategoryId();
        this.sigProductImg=sigProductImg;
        this.ordinalProductImg=ordinalProductImg;
        this.loginId=loginId;
    }
}
