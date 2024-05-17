package com.auction.usedauction.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "message")
@CompoundIndex(def = "{'chatRoomId': 1, 'createdDate': -1}")
@Getter
public class Chat {

    @Id
    private String id;

    private Long chatRoomId;

    private String sender;

    private String message;

    private LocalDateTime createdDate;

    @Builder
    public Chat(Long chatRoomId, String sender, String message, LocalDateTime createdDate) {
        this.chatRoomId = chatRoomId;
        this.sender = sender;
        this.message = message;
        this.createdDate = createdDate;
    }
}
