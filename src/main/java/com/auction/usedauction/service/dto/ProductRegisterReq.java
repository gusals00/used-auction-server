package com.auction.usedauction.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductRegisterReq {

    private String name;
    private String info;
    private Long categoryId;
    private Long memberId;
    private String status;
    private LocalDateTime auctionStartDate;
    private LocalDateTime auctionEndDate;
    private Long buyNowPrice;
    private Long startPrice;
    private Long priceUnit;

    private List<MultipartFile> img;

    @NotNull
    private MultipartFile sigImg;
}
