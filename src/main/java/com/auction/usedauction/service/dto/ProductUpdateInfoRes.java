package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.file.File;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateInfoRes {

    @Schema(description = "상품 이름", example = "맛있는 사과입니다")
    private String productName;

    @Schema(description = "상품 정보", example = "맛있는 사과입니다 맛있습니다")
    private String info;

    @Schema(description = "카테고리 ID", example = "2")
    private Long categoryId;

    @Schema(description = "경매 종료 날짜", example = "2022-12-11 12:01")
    private String auctionEndDate;

    @Schema(description = "시작가격", example = "10000")
    private int startPrice;

    @Schema(description = "입찰 단위가", example = "10000")
    private int priceUnit;

    @Schema(description = "수정 가능한 상태인가", example = "true")
    private boolean isPossibleUpdate;

    @Schema(description = "대표 사진")
    private ImageInfoRes sigImg;
    private List<ImageInfoRes> ordinalImgList;

    public ProductUpdateInfoRes(Product product, boolean isPossibleUpdate) {
        this.productName = product.getName();
        this.info = product.getInfo();
        this.categoryId = product.getCategory().getId();
        Auction auction = product.getAuction();
        this.auctionEndDate = auction.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.startPrice = auction.getStartPrice();
        this.priceUnit = auction.getPriceUnit();
        this.isPossibleUpdate = isPossibleUpdate;

        File sigImage = product.getSigImage();
        this.sigImg = new ImageInfoRes(sigImage.getOriginalName(), sigImage.getFullPath());
        this.ordinalImgList = product.getOrdinalImageList()
                .stream()
                .map(ordinalImg -> new ImageInfoRes(ordinalImg.getOriginalName(), ordinalImg.getFullPath()))
                .collect(toList());
    }
}
