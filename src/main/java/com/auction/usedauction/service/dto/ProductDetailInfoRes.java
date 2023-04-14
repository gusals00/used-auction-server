package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.file.File;
import com.auction.usedauction.util.AuctionProgressUtil;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "상품 이름",example = "자료구조 책 팝니다")
    private String productName;
    @Schema(description = "상품 정보",example = "자료구조 새 책입니다.")
    private String info;
    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;
    @Schema(description = "판매자 아이디(pk)",example = "100")
    private Long memberId;
    @Schema(description = "판매자 로그인 아이디",example = "lovesoe1234")
    private String nickname;
    @Schema(description = "경매 종료 날짜",example = "2023-10-12 12:01")
    private String auctionEndDate;
    @Schema(description = "현재 가격",example = "10000")
    private int nowPrice;
    @Schema(description = "시작 가격",example = "1000")
    private int startPrice;
    @Schema(description = "입찰 단위 가격",example = "1000")
    private int priceUnit;
    @Schema(description = "조회수",example = "7")
    private int viewCount;
    @Schema(description = "경매상태",example = "경매 중")
    private String status;
    private ImageInfoRes sigImg;
    private List<ImageInfoRes> ordinalImgList;

    public ProductDetailInfoRes(Product product) {
        this.productName = product.getName();
        this.info = product.getInfo();
        this.categoryName = product.getCategory().getName();
        this.memberId = product.getMember().getId();
        this.nickname = product.getMember().getName();
        this.viewCount = product.getViewCount();

        Auction auction = product.getAuction();
        this.auctionEndDate = auction.getAuctionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.nowPrice = auction.getNowPrice();
        this.startPrice = auction.getStartPrice();
        this.priceUnit = auction.getPriceUnit();
        this.status = AuctionProgressUtil.changeAuctionStatusToName(auction.getStatus());

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
        @Schema(description = "사진 원본 이름",example = "객체지향.jpg")
        private String originalName;
        @Schema(description = "사진 저장 경로",example = "http://sfieuhfe.sfee.jpg")
        private String path;
    }

}
