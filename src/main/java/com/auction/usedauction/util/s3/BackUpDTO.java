package com.auction.usedauction.util.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BackUpDTO {

    private String command;
    private String path;
}
