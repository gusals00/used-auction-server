package com.auction.usedauction.web.dto;

import com.auction.usedauction.domain.ChatRoom;
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

    @Schema(description = "안읽은 메세지 수", example = "10")
    private Long unReadMessages;

    public ChatRoomRes(ChatRoom chatRoom, Long memberId) {
        this.chatRoomId = chatRoom.getId();
        this.roomName = chatRoom.getProduct().getName();
        this.unReadMessages = chatRoom.getChatMessages()
                .stream()
                .filter(chatMessage ->  !chatMessage.isReadOrNot() && (chatMessage.getMember().getId() != memberId))
                .count();
        int messageSize = chatRoom.getChatMessages().size();
        if(messageSize == 0) {
            this.recentMessage = "";
        } else {
            this.recentMessage = chatRoom.getChatMessages().get(messageSize-1).getMessage();
        }
    }
}
