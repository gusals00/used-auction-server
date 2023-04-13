package com.auction.usedauction.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuctionRegisterDTO {

    private LocalDateTime auctionEndDate;
    private int startPrice;
    private int priceUnit;
}
