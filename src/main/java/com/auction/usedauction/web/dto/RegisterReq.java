package com.auction.usedauction.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {

    private String name;

    private String loginId;

    private String password;

    private String birth;

    private String email;

    private String phoneNumber;

    private String code;
}
