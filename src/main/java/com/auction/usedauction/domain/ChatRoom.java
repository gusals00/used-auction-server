package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatRoom extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDateTime buyerLastLeftAt;

    private LocalDateTime sellerLastLeftAt;

    @Builder
    public ChatRoom(Member member, Product product) {
        this.member = member;
        this.product = product;

        buyerLastLeftAt = LocalDateTime.now();
        sellerLastLeftAt = LocalDateTime.now();
    }

    public void updateLastLeftAtToNow(String loginId) {
        if(member.getLoginId().equals(loginId)) {
            buyerLastLeftAt = LocalDateTime.now();
        } else {
            sellerLastLeftAt = LocalDateTime.now();
        }
    }

}
