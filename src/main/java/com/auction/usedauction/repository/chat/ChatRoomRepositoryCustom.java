package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.ChatRoom;
import com.auction.usedauction.repository.dto.ChatRoomsDTO;

import java.util.List;

public interface ChatRoomRepositoryCustom {
    List<ChatRoom> findChatRoomsByMemberId(Long memberId);
    List<ChatRoomsDTO> findChatRoomsByMemberLoginId(String loginId);
    boolean existsUnReadMessages(Long roomId, String loginId);
}
