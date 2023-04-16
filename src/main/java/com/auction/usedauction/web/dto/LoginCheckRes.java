package com.auction.usedauction.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginCheckRes {

    private Boolean status;

    private String loginId;

    private String name;
}
