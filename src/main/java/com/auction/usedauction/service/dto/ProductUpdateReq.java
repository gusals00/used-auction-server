package com.auction.usedauction.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateReq {

    @NotBlank
    private String productName;
    @NotBlank
    private String info;
    @NotNull
    private Long categoryId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime auctionEndDate;
    @Min(1000)
    private Integer buyNowPrice;
    @Min(100)
    @NotNull
    private int startPrice;

    @Min(1000)
    @NotNull
    private int priceUnit;
    private List<MultipartFile> img;
    @NotNull
    private MultipartFile sigImg;
}
