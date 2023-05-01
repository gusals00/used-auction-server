package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomCreateInfoDTO {

    private Long chatRoomId;

    private String roomName;

    @QueryProjection
    public ChatRoomCreateInfoDTO(Long chatRoomId, String roomName) {
        this.chatRoomId = chatRoomId;
        this.roomName = roomName;
    }
}
