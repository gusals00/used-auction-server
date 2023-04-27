package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageWebSocketRes {

    @Schema(description = "메세지 타입", example = "ENTER")
    private MessageType type;

    @Schema(description = "발신자", example = "현민")
    private String sender;

    @Schema(description = "메세지", example = "안녕하세요")
    private String message;

    @Schema(description = "읽음 여부", example = "true")
    private boolean readOrNot;

    @Schema(description = "보낸 시간", example = "2023-10-12 12:01:00")
    private String sentTime;

    public ChatMessageWebSocketRes(MessageType type, String sender, String message, boolean readOrNot, String sentTime) {
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.readOrNot = readOrNot;
        this.sentTime = sentTime;
    }
}
