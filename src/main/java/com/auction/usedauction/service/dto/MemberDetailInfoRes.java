package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class MemberDetailInfoRes {

    private String name;

    private String loginId;

    private String birth;

    private String email;

    private String phoneNumber;

    private String createdDate;

    public MemberDetailInfoRes(Member member) {
        this.name = member.getName();
        this.loginId = member.getLoginId();
        this.birth = member.getBirth();
        this.email = member.getEmail();
        this.phoneNumber = member.getPhoneNumber();
        this.createdDate = member.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));;
    }
}
