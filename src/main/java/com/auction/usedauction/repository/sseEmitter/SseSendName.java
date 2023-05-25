package com.auction.usedauction.repository.sseEmitter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SseSendName {

    CONNECT("SSE 연결 확인"),
    SEND_BID_DATA("입찰 가격 데이터 전달"),
    SEND_ROOM_DATA("채팅방 데이터 전달"),
    SEND_ROOM_ENTER_DATA("채팅방 입장 데이터 전달"),
    SEND_NEW_ROOM_DATA("새로운 채팅방 데이터 전달"),
    SEND_NOTIFICATION_DATA("알림 데이터 전달");

    private final String description;
}