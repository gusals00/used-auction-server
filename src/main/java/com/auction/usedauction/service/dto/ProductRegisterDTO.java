package com.auction.usedauction.service.dto;

import com.auction.usedauction.util.UploadFIleDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

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
    private Long buyNowPrice;
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
        this.buyNowPrice = registerReq.getBuyNowPrice();
        this.startPrice = registerReq.getStartPrice().intValue();
        this.priceUnit = registerReq.getPriceUnit().intValue();
        this.sigProductImg=sigProductImg;
        this.ordinalProductImg=ordinalProductImg;
        this.loginId=loginId;
    }
}
