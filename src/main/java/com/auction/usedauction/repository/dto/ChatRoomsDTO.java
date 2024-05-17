package com.auction.usedauction.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomsDTO {

    private Long chatRoomId;

    private String roomName;

    private boolean isSeller;

    @QueryProjection
    public ChatRoomsDTO(Long chatRoomId, String roomName, boolean isSeller) {
        this.chatRoomId = chatRoomId;
        this.roomName = roomName;
        this.isSeller = isSeller;
    }
}
