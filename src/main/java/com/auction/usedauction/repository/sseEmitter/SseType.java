package com.auction.usedauction.repository.sseEmitter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SseType {

    BID("입찰 가격"),
    CHAT_LIST("채팅방 리스트"),
    NOTIFICATION("알림");

    private final String description;
}
