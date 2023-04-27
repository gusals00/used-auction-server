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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createRoom(Long productId, String loginId) {
        Member buyer = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(loginId, MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findByIdAndProductStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 접속인원 증가
        chatRoom.addUserCount();

        // 메세지 읽음표시로 바꾸기
        chatMessageRepository.updateMessages(loginId, roomId);
    }

    @Transactional
    public void leaveRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 접속인원 감소
        chatRoom.minusUserCount();
    }
}
