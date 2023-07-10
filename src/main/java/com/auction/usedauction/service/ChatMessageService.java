package com.auction.usedauction.service;

import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.util.RedisUtil;
import com.auction.usedauction.web.dto.ChatMessageSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.auction.usedauction.util.RedisConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;
    private final RedisUtil redisUtil;

    public boolean addChat(Long chatRoomId, String loginId, String message) {
        Long memberId = memberRepository.findIdByLoginId(loginId);

        boolean readOrNot = false;

        // 채팅방에 둘다 들어와 있으면 읽음 표시로 보냄
        if(chatRoomService.getUserCount(chatRoomId) == 2) {
            readOrNot = true;
        }

        ChatMessageSaveDTO chatData = new ChatMessageSaveDTO(chatRoomId, memberId, message, readOrNot, LocalDateTime.now());
        redisUtil.addSet(NEW_CHAT + "_" + chatRoomId, chatData);

        return readOrNot;
    }


}
