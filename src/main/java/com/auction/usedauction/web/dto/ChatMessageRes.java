package com.auction.usedauction.web.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRes {

    @Schema(description = "발신자", example = "현민")
    private String sender;

    @Schema(description = "메세지", example = "안녕하세요")
    private String message;

    @Schema(description = "보낸 시간", example = "2023-10-12 12:01:00")
    private String sentTime;

    @Builder
    public ChatMessageRes(String sender, String message, String sentTime) {
        this.sender = sender;
        this.message = message;
        this.sentTime = sentTime;
    }

    public ChatMessageRes(MessageDTO messageDTO) {
        this.sender = messageDTO.getSender();
        this.message = messageDTO.getMessage();
        this.sentTime = messageDTO.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
