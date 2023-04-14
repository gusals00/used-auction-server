package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.AuctionHistoryStatus;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageAuctionHistoryPageContentRes {

    @Schema(description = "상품 ID",example = "1")
    private Long productId;

    @Schema(description = "카테고리 이름",example = "디지털 기기")
    private String categoryName;

    @Schema(description = "상품 이름",example = "공학수학 책 팔아요")
    private String productName;

    @Schema(description = "입찰/낙찰 가격", example = "50000")
    private Integer bidPrice;

    @Schema(description = "입찰/낙찰 날짜", example = "2023-10-12 12:01")
    private String createdDate;

    @Schema(description = "경매내역 상태", example = "BID")
    private AuctionHistoryStatus status;

    @QueryProjection
    public MyPageAuctionHistoryPageContentRes(Long productId, String categoryName, String productName, Integer bidPrice, LocalDateTime createdDate, AuctionHistoryStatus status) {
        this.productId = productId;
        this.categoryName = categoryName;
        this.productName = productName;
        this.bidPrice = bidPrice;
        this.createdDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.status = status;
    }
}
