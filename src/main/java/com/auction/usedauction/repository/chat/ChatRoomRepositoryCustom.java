package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.ChatRoom;

import java.util.List;

public interface ChatRoomRepositoryCustom {
    List<ChatRoom> findChatRoomsByMemberId(Long memberId);
    List<ChatRoom> findChatRoomsByMemberLoginId(String loginId);
    boolean existsUnReadMessages(Long roomId, String loginId);
}
