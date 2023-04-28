package com.auction.usedauction.repository.sseEmitter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SseSendName {

    CONNECT("SSE 연결 확인"),
    SEND_BID_DATA("입찰 가격 데이터 전달"),
    SEND_CHAT_LIST_DATA("채팅방 리스트 데이터 전달");

    private final String description;
}