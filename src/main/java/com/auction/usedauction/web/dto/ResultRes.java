package com.auction.usedauction.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultRes<T> {

    private T result;

}
