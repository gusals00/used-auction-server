package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.ChatMessageService;
import com.auction.usedauction.service.ChatRoomService;
import com.auction.usedauction.service.sseEmitter.SseEmitterService;
import com.auction.usedauction.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat-message")
@Tag(name = "chatting message controller", description = "채팅 메세지 관련 api")
public class ChatMessageController {

    private final SimpMessageSendingOperations template;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SseEmitterService sseEmitterService;

    @MessageMapping("/message")
    @Operation(summary = "메세지 처리")
    public void message(ChatMessageDTO messageDTO, Principal principal) {
        chatMessageService.send("chat", messageDTO);
    }

    @GetMapping("/chats/{roomId}")
    @Operation(summary = "채팅 메세지 리스트 조회 메서드")
    public ResultRes<List<ChatMessageRes>> getMessageList(@AuthenticationPrincipal User user, @PathVariable Long roomId,
                                                          @RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastTime,
                                                          @RequestParam(defaultValue = "20") Integer pageSize) {

        return new ResultRes(chatMessageService.getChatMessage(roomId, lastTime, pageSize));


        //PageRequest pageRequest = PageRequest.of(page, size);
        // return chatMessageQueryService.getMessageList(pageRequest, roomId, user.getUsername());
    }

    private ChatMessageWebSocketRes createMessageRes(ChatMessageDTO messageReq, boolean isRead) {
        return new ChatMessageWebSocketRes(messageReq.getType(), messageReq.getSender(), messageReq.getMessage(), isRead, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
