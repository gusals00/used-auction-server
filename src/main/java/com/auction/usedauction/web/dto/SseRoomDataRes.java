package com.auction.usedauction.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SseRoomDataRes {

    private Long chatRoomId;

    private String recentMessage;

    private String recentSender;

    private boolean unReadMessages;

}
