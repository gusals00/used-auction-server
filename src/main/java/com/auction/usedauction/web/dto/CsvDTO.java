package com.auction.usedauction.web.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CsvDTO {
    @CsvBindByName
    private String loginId;
    @CsvBindByName
    private String accessToken;
    @CsvBindByName
    private int price;
    @CsvBindByName
    private Long auctionId;
}
