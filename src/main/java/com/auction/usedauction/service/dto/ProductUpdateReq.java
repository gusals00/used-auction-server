package com.auction.usedauction.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateReq {

    @NotBlank
    @Schema(description = "변경할 상품 이름", example = "맛있는 사과입니다")
    private String productName;

    @NotBlank
    @Schema(description = "변경할 상품 정보", example = "맛있는 사과입니다 맛있습니다")
    private String info;

    @NotNull
    @Schema(description = "카테고리 ID", example = "2")
    private Long categoryId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Future
    @NotNull
    @Schema(description = "경매 종료 날짜", example = "2022-12-11 12:01")
    private LocalDateTime auctionEndDate;

    @Min(100)
    @NotNull
    @Schema(description = "시작가격", example = "10000")
    private int startPrice;

    @Min(1000)
    @NotNull
    @Schema(description = "입찰 단위가", example = "10000")
    private int priceUnit;

    @NotNull
    private List<MultipartFile> imgList;
    @NotNull
    @Schema(description = "수정할 대표 사진")
    private MultipartFile sigImg;
}
