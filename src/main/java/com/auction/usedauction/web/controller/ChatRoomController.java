package com.auction.usedauction.web.controller;

import com.auction.usedauction.service.ChatRoomService;
import com.auction.usedauction.service.sseEmitter.SseEmitterService;
import com.auction.usedauction.service.query.ChatRoomQueryService;
import com.auction.usedauction.web.dto.ChatRoomRes;
import com.auction.usedauction.web.dto.MessageRes;
import com.auction.usedauction.web.dto.ResultRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room")
@Tag(name = "chatting room controller", description = "채팅방 관련 api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final SseEmitterService sseEmitterService;

    @PostMapping("/{productId}")
    @Operation(summary = "채팅방 생성 메서드")
    public ResultRes<MessageRes> createRoom(@PathVariable Long productId, @AuthenticationPrincipal User user) {
        Long roomId = chatRoomService.createRoom(productId, user.getUsername());

        sseEmitterService.sendNewRoomData(roomId);

        return new ResultRes(new MessageRes("채팅방 생성 성공"));
    }

    @GetMapping
    @Operation(summary = "채팅방 리스트 조회 메서드")
    public ResultRes<List<ChatRoomRes>> getRoomList(@AuthenticationPrincipal User user) {
        return new ResultRes(chatRoomQueryService.getRoomLists(user.getUsername()));
    }
}
