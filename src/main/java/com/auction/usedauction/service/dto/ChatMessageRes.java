package com.auction.usedauction.service.dto;

import com.auction.usedauction.domain.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "위치", example = "outgoing")
    private String direction;

    @Schema(description = "메세지", example = "안녕하세요")
    private String message;

    @Schema(description = "읽음 여부", example = "true")
    private boolean readOrNot;

    @Schema(description = "보낸 시간", example = "2023-10-12 12:01:00")
    private String sentTime;

    public ChatMessageRes(ChatMessage chatMessage, String loginId) {
        this.sender = chatMessage.getMember().getName();
        this.message = chatMessage.getMessage();
        this.readOrNot = chatMessage.isReadOrNot();
        this.sentTime = chatMessage.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        if(chatMessage.getMember().getLoginId().equals(loginId)) {
            this.direction = "outgoing";
        } else {
            this.direction = "incoming";
        }
    }
}
