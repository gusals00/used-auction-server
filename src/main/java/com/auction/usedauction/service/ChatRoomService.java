package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.chat.ChatMessageJdbcRepository;
import com.auction.usedauction.repository.chat.ChatMessageRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.util.RedisUtil;
import com.auction.usedauction.web.dto.ChatMessageSaveDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Set;

import static com.auction.usedauction.exception.error_code.ChatErrorCode.*;
import static com.auction.usedauction.util.RedisConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;
    private final ChatMessageJdbcRepository chatMessageJdbcRepository;

    @Transactional
    public Long createRoom(Long productId, String loginId) {
        Member buyer = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(loginId, MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findByIdAndProductStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 중복체크
        if(chatRoomRepository.existsByProductId(productId)) {
            throw new CustomException(CHAT_ROOM_DUPLICATED);
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .member(buyer)
                .product(product)
                .build();

        chatRoomRepository.save(chatRoom);
        return chatRoom.getId();
    }

    @Transactional
    public void enterRoom(Long roomId, String loginId) {
        int userCount = getUserCount(roomId);
        if(userCount < 2) {
            addUserCount(roomId);
        }

        // 채팅방 인원이 2명이 될 때 채팅 데이터 DB에 저장
        if(userCount == 1) {
            writeBack(roomId);
        }

        // 메세지 읽음표시로 바꾸기
        chatMessageRepository.updateMessages(loginId, roomId);
    }

    @Transactional
    public void leaveRoom(Long roomId) {
        int userCount = getUserCount(roomId);

        // 채팅방 인원이 0명이 될 때 채팅 데이터 DB에 저장
        if(userCount == 1) {
            writeBack(roomId);
        }

        // 채팅방 접속인원 감소
        minusUserCount(roomId);
    }

    public int getUserCount(Long roomId) {
        String result = redisUtil.getData(USER_COUNT + "_" + roomId);
        return result==null ? 0 : Integer.parseInt(result);
    }

    public Long addUserCount(Long roomId) {
        return redisUtil.incrementData(USER_COUNT + "_" + roomId);
    }

    public Long minusUserCount(Long roomId) {
        return redisUtil.decrementData(USER_COUNT + "_" + roomId);
    }

    // redis에 있는 채팅 데이터를 DB에 저장하고 redis에 있는 채팅 데이터 삭제
    @Transactional
    public void writeBack(Long roomId) {
        Set<ChatMessageSaveDTO> chatData = redisUtil.getSet(NEW_CHAT + "_" + roomId);
        if(chatData.size() != 0) {
            chatMessageJdbcRepository.chatMessageBatchInsert(chatData);
            redisUtil.deleteChat(NEW_CHAT + "_" + roomId);
        }
    }
}
