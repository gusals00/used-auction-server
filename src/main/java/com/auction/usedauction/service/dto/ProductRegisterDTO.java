package com.auction.usedauction.service.dto;

import com.auction.usedauction.util.UploadFIleDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductRegisterDTO {

    private String name;
    private String info;
    private Long categoryId;
    private LocalDateTime auctionEndDate;
    private int startPrice;
    private int priceUnit;

    private UploadFIleDTO sigProductImg;
    private List<UploadFIleDTO> ordinalProductImg;
    private String loginId;

    public ProductRegisterDTO(ProductRegisterReq registerReq,UploadFIleDTO sigProductImg,List<UploadFIleDTO> ordinalProductImg,String loginId) {
        this.name = registerReq.getName();
        this.info = registerReq.getInfo();
        this.categoryId = registerReq.getCategoryId();
        this.auctionEndDate = registerReq.getAuctionEndDate();
        this.startPrice = registerReq.getStartPrice().intValue();
        this.priceUnit = registerReq.getPriceUnit().intValue();
        this.sigProductImg=sigProductImg;
        this.ordinalProductImg=ordinalProductImg;
        this.loginId=loginId;
    }
}
