package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private String loginId;

    private String birth;

    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Builder
    public User(String name, String loginId, String birth, String email, String phoneNumber) {
        this.name = name;
        this.loginId = loginId;
        this.birth = birth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        status = UserStatus.EXIST;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }
}
