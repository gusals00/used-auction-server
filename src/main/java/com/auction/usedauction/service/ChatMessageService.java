package com.auction.usedauction.service;

import com.auction.usedauction.domain.ChatMessage;
import com.auction.usedauction.domain.ChatRoom;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ChatErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.chat.ChatMessageRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public boolean saveMessage(Long chatRoomId, String loginId, String message) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        boolean read = false;

        // 채팅방에 둘다 들어와 있으면 읽음 표시로 보냄
        if(chatRoom.getUserCount() == 2) {
            read = true;
        }

        ChatMessage chatMessage = createChatMessage(message, chatRoom, member, read);
        chatMessageRepository.save(chatMessage);

        return read;
    }

    @Transactional
    public void readMessages(String loginId, Long roomId) {
        chatMessageRepository.updateMessages(loginId, roomId);
    }

    private ChatMessage createChatMessage(String message, ChatRoom chatRoom, Member member, boolean read) {
        return ChatMessage.builder()
                .message(message)
                .chatRoom(chatRoom)
                .member(member)
                .readOrNot(read)
                .build();
    }
}
