package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.ChatMessageService;
import com.auction.usedauction.service.ChatRoomService;
import com.auction.usedauction.service.SseEmitterService;
import com.auction.usedauction.service.dto.ChatMessageRes;
import com.auction.usedauction.service.query.ChatMessageQueryService;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat-message")
@Tag(name = "채팅 메세지 컨트롤러", description = "채팅 메세지 관련 api")
public class ChatMessageController {

    private final SimpMessageSendingOperations template;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageQueryService chatMessageQueryService;
    private final SseEmitterService sseEmitterService;

    @MessageMapping("/message")
    @Operation(summary = "메세지 처리")
    public void message(ChatMessageDTO messageDTO, Principal principal) {
        boolean isRead = false;
        if(messageDTO.getType().equals(MessageType.ENTER)) { // 입장 메세지
            messageDTO.setMessage(messageDTO.getSender());

            chatRoomService.enterRoom(messageDTO.getChatRoomId(), principal.getName()); // 채팅방 입장 처리

        } else if(messageDTO.getType().equals(MessageType.TALK)){ // 대화 메세지
            isRead = chatMessageService.saveMessage(messageDTO.getChatRoomId(), principal.getName(), messageDTO.getMessage()); // 메시지 저장

            sseEmitterService.sendUpdatedRoomData(messageDTO, principal.getName(), isRead); // sse 채팅방 데이터 전송
        }

        // 메시지 전송
        template.convertAndSend("/sub/room/" + messageDTO.getChatRoomId(), createMessageRes(messageDTO, isRead));
    }

    @GetMapping("/chats/{roomId}")
    @Operation(summary = "채팅 메세지 리스트 조회 메서드")
    public PageListRes<ChatMessageRes> getMessageList(@AuthenticationPrincipal User user, @PathVariable Long roomId,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "15") Integer size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        return chatMessageQueryService.getMessageList(pageRequest, roomId, user.getUsername());
    }

    private ChatMessageWebSocketRes createMessageRes(ChatMessageDTO messageReq, boolean isRead) {
        return new ChatMessageWebSocketRes(messageReq.getType(), messageReq.getSender(), messageReq.getMessage(), isRead, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
