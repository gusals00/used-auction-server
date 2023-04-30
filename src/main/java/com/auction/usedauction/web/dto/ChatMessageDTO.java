package com.auction.usedauction.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private Long chatRoomId;

    private String sender;

    private String message;

    private MessageType type;

}
