package com.auction.usedauction.web.dto;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "상품 제목",example = "공학수학 팔아요")
    private String name;
    @NotEmpty
    @Schema(description = "상품 정보",example = "이책은 새책입니다")
    private String info;
    @NotNull
    @Schema(description = "카테고리 ID",example = "1")
    private Long categoryId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "경매 종료 날짜",defaultValue = "2023-10-03 12:10:00")
    private LocalDateTime auctionEndDate;
    @Min(1000)
    @Schema(description = "즉시 구매가",example = "3000",minimum = "1000",nullable = true)
    private Long buyNowPrice;
    @Min(100)
    @Schema(description = "경매 시작가",example = "1000",minimum = "100")
    private Long startPrice;
    @Min(1000)
    @Schema(description = "입찰 단위가",example = "1000",minimum = "1000")
    private Long priceUnit;

    @Schema(description = "일반 이미지 파일들",nullable = true)
    private List<MultipartFile> imgList;

    @NotNull
    @Schema(description = "대표 이미지 파일들")
    private MultipartFile sigImg;
}
