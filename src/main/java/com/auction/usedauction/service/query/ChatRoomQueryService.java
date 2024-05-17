package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.Chat;
import com.auction.usedauction.domain.ChatRoom;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.MemberStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ChatErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.chat.ChatRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.repository.dto.ChatRoomsDTO;
import com.auction.usedauction.web.dto.ChatRoomRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;

    public List<ChatRoomRes> getRoomLists(String loginId) {
        List<ChatRoomsDTO> chatRooms = chatRoomRepository.findChatRoomsByMemberLoginId(loginId);

        return chatRooms.stream()
                .map(chatRoom -> {
                    Chat chat = chatRepository.findTopByChatRoomIdOrderByCreatedDateDesc(chatRoom.getChatRoomId())
                            .orElse(new Chat(chatRoom.getChatRoomId(), "", "", null));

                    LocalDateTime lastLeftTime = chatRoom.isSeller() ? chatRoomRepository.findSellerLastLeftAt(chatRoom.getChatRoomId()) : chatRoomRepository.findBuyerLastLeftAt(chatRoom.getChatRoomId());

                    Long unReadMessages = chatRepository.countByChatRoomIdAndCreatedDateAfter(chatRoom.getChatRoomId(), lastLeftTime);

                    return new ChatRoomRes(chatRoom.getChatRoomId(), chatRoom.getRoomName(), chat.getMessage(), chat.getSender(), unReadMessages);
                })
                .collect(Collectors.toList());
    }
}
