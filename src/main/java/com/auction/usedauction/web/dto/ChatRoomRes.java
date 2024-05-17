package com.auction.usedauction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRes {

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "채팅방 이름", example = "공학수학 책 팔아요")
    private String roomName;

    @Schema(description = "최근 메세지", example = "안녕하세요")
    private String recentMessage;

    @Schema(description = "최근 메세지 발신자", example = "현민")
    private String recentSender;

    @Schema(description = "안읽은 메세지 수", example = "10")
    private Long unReadMessages;
}
