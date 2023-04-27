package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.ChatMessageService;
import com.auction.usedauction.service.dto.ChatMessageRes;
import com.auction.usedauction.service.query.ChatMessageQueryService;
import com.auction.usedauction.web.dto.ChatMessageReq;
import com.auction.usedauction.web.dto.ChatMessageWebSocketRes;
import com.auction.usedauction.web.dto.MessageType;
import com.auction.usedauction.web.dto.PageListRes;
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
    private final ChatMessageQueryService chatMessageQueryService;

    @MessageMapping("/message")
    @Operation(summary = "메세지 처리")
    public void message(ChatMessageReq messageReq, Principal principal) {
        boolean isRead = false;
        if(messageReq.getType().equals(MessageType.ENTER)) {
            messageReq.setMessage(messageReq.getSender());
        } else if(messageReq.getType().equals(MessageType.TALK)){
            isRead = chatMessageService.saveMessage(messageReq.getChatRoomId(), principal.getName(), messageReq.getMessage());
        }

        template.convertAndSend("/sub/room/" + messageReq.getChatRoomId(), createMessageRes(messageReq, isRead));
    }

    @GetMapping("/chats/{roomId}")
    @Operation(summary = "채팅 메세지 리스트 조회 메서드")
    public PageListRes<ChatMessageRes> getMessageList(@AuthenticationPrincipal User user, @PathVariable Long roomId,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "15") Integer size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        return chatMessageQueryService.getMessageList(pageRequest, roomId, user.getUsername());
    }

    private ChatMessageWebSocketRes createMessageRes(ChatMessageReq messageReq, boolean isRead) {
        return new ChatMessageWebSocketRes(messageReq.getType(), messageReq.getSender(), messageReq.getMessage(), isRead, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
