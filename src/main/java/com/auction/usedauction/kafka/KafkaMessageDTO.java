package com.auction.usedauction.kafka;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class KafkaMessageDTO {

    private Long chatRoomId;

    private String sender;

    private String message;

    private LocalDateTime createdDate;

    private boolean isRead;

    @Builder
    public KafkaMessageDTO(Long chatRoomId, String sender, String message, LocalDateTime createdDate, boolean isRead) {
        this.chatRoomId = chatRoomId;
        this.sender = sender;
        this.message = message;
        this.createdDate = createdDate;
        this.isRead = isRead;
    }
}
