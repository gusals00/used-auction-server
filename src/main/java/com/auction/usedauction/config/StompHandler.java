package com.auction.usedauction.config;

import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.security.TokenProvider;
import com.auction.usedauction.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.auction.usedauction.exception.error_code.ChatErrorCode.*;
import static com.auction.usedauction.exception.error_code.SecurityErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final ChatRoomService chatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(StompCommand.CONNECT == accessor.getCommand()) {
            log.info("CONNECT 호출");
            String token = accessor.getFirstNativeHeader("Authorization");

            if(StringUtils.hasText(token) && token.startsWith("Bearer ")){
                token = token.substring(7);

                if(tokenProvider.isValidTokenStomp(token)) {
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    accessor.setUser(authentication);
                }
            } else {
                throw new CustomException(ACCESS_DENIED);
            }
        } else if(StompCommand.SUBSCRIBE == accessor.getCommand()) {
            log.info("SUBSCRIBE 호출");
            Long roomId = Long.valueOf(getRoomId((String) message.getHeaders().get("simpDestination"))); // 채팅방 아이디 가져오기

            chatRoomService.enterRoom(roomId, accessor.getUser().getName()); // 채팅방 입장 처리

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes(); // 웹소켓 세션에 채팅방 아이디 저장
            sessionAttributes.put(accessor.getSessionId(), roomId);
            accessor.setSessionAttributes(sessionAttributes);

        } else if(StompCommand.DISCONNECT == accessor.getCommand()) {
            log.info("DISCONNECT 호출");
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            Long roomId = (Long) sessionAttributes.get(accessor.getSessionId()); // 채팅방 아이디 가져오기

            chatRoomService.leaveRoom(roomId); // 채팅방 퇴장 처리
        }

        return message;
    }

    private String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if(lastIndex != -1) {
            return destination.substring(lastIndex+1);
        } else {
            throw new CustomException(INVALID_ROOM_ID);
        }
    }
}
