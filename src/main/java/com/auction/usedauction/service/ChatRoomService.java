package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ChatErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.chat.ChatMessageRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.util.RedisConstants;
import com.auction.usedauction.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private final Long roomListExpireTime = 600L; // 10분

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
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        // 채팅방 접속인원 증가
        if(chatRoom.getUserCount() < 2) {
            chatRoom.addUserCount();
        }

        // 메세지 읽음표시로 바꾸기
        chatMessageRepository.updateMessages(loginId, roomId);
    }

    @Transactional
    public void leaveRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        // 채팅방 접속인원 감소
        chatRoom.minusUserCount();
    }

    // redis에 입장중인 방 목록 저장
    public List<String> addJoinedRoomListToRedis(String loginId) {
        List<String> joinedRoomList = chatRoomRepository.findChatRoomsByMemberLoginId(loginId)
                .stream()
                .map(chatRoom -> chatRoom.getId().toString())
                .toList();

        redisUtil.setList(ROOM_LIST + loginId, joinedRoomList, roomListExpireTime, TimeUnit.SECONDS);

        return joinedRoomList;
    }

    // redis에 새로 생성된 방 저장
    public void addNewRoomToRedis(String loginId, Long roomId) {
        List<String> list = redisUtil.getList(ROOM_LIST + loginId);

        if(list != null) {
            redisUtil.addList(ROOM_LIST + loginId, roomId.toString());
        }
    }
}
