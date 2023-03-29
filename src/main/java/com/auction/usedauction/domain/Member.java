package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String loginId;

    private String password;

    private String birth;

    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @ManyToMany // 수정 예정
    @JoinTable(
            name = "member_authority",
            joinColumns = {@JoinColumn(name = "member_id", referencedColumnName = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;

    @Builder
    public Member(String name, String loginId, String password, String birth, String email, String phoneNumber, Set<Authority> authorities) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.birth = birth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = MemberStatus.EXIST;
        this.authorities = authorities;
    }

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }
}
