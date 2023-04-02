package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.file.File;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.*;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailInfoRes {

    private String productName;
    private String info;
    private String categoryName;
    private Long memberId;
    private String memberLoginId;
    private String auction_end_date;
    private int buyNowPrice;
    private int nowPrice;
    private int startPrice;
    private int priceUnit;
    private int viewCount;
    private ImageInfoRes sigImg;
    private List<ImageInfoRes> ordinalImgList;

    public ProductDetailInfoRes(Product product) {
        this.productName = product.getName();
        this.info = product.getInfo();
        this.categoryName = product.getCategory().getName();
        this.memberId = product.getMember().getId();
        this.memberLoginId = product.getMember().getLoginId();
        this.auction_end_date = product.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.buyNowPrice = product.getBuyNowPrice();
        this.nowPrice = product.getNowPrice();
        this.startPrice = product.getStartPrice();
        this.priceUnit = product.getPriceUnit();
        this.viewCount = product.getViewCount();

        File sigImage = product.getSigImage();
        this.sigImg = new ImageInfoRes(sigImage.getOriginalName(), sigImage.getFullPath());
        this.ordinalImgList = product.getOrdinalImageList()
                .stream()
                .map(ordinalImg->new ImageInfoRes(ordinalImg.getOriginalName(),ordinalImg.getFullPath()))
                .collect(toList());
    }

    @Setter
    @Getter
    @AllArgsConstructor
    static class ImageInfoRes {
        private String originalName;
        private String path;
    }

}
