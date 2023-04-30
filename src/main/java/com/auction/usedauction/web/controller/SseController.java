package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.repository.sseEmitter.SseEmitterRepository;
import com.auction.usedauction.repository.sseEmitter.SseSendName;
import com.auction.usedauction.repository.sseEmitter.SseType;
import com.auction.usedauction.service.SseEmitterService;
import com.auction.usedauction.service.dto.SseSendDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Tag(name = "Sse 컨트롤러", description = "Sse 관련 api")
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final SseEmitterRepository sseEmitterRepository;
    private final ProductRepository productRepository;

    @Operation(summary = "sse 입찰 금액 연결 메서드")
    @GetMapping(value = "/bid-connect/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Void> connectBid(@PathVariable Long productId) {
        Long timeout = 1000 * 60 * 5L; //5분
        // 연결
        String id = sseEmitterService.connect(SseType.BID, productId, timeout);
        SseEmitterDTO findEmitter = sseEmitterRepository.findByEmitterId(id);

        // 상품 경매 현재가 전송
        Integer nowPrice = productRepository.findNowPriceByProductId(productId);
        if (nowPrice != null) {
            sseEmitterService.send(new SseSendDTO(findEmitter, SseSendName.SEND_BID_DATA, nowPrice));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/chat-connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connectChatList(@AuthenticationPrincipal User user) {
        Long timeout = 1000 * 60 * 10L; //10분
        // 연결
        String id = sseEmitterService.connect(SseType.CHAT_LIST, user.getUsername(), timeout);
        SseEmitterDTO findEmitter = sseEmitterRepository.findByEmitterId(id);

        // 채팅방 리스트 전송 추가해 현민아...
        List<SseEmitterDTO> findEmitters = sseEmitterRepository.findAllByTypeAndLoginId(SseType.CHAT_LIST, user.getUsername());

        return ResponseEntity.ok(findEmitter.getSseEmitter());
    }


}
