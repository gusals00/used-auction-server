package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatMessage{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    private String message;

    private boolean readOrNot;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    @Builder
    public ChatMessage(String message, boolean readOrNot, ChatRoom chatRoom, Member member, LocalDateTime createdDate) {
        this.message = message;
        this.readOrNot = readOrNot;
        this.member = member;
        this.chatRoom = chatRoom;
        this.createdDate = createdDate;

        if(chatRoom != null) {
            chatRoom.getChatMessages().add(this);
        }
    }
}
