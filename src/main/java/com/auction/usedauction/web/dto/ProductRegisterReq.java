package com.auction.usedauction.web.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRegisterReq {

    @NotEmpty
    private String name;
    @NotEmpty
    private String info;
    @NotNull
    private Long categoryId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auctionEndDate;
    @Range(min = 1000)
    private Long buyNowPrice;
    @Min(100)
    private Long startPrice;
    @Min(1000)
    private Long priceUnit;

    private List<MultipartFile> imgList;

    @NotNull
    private MultipartFile sigImg;
}
