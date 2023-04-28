package com.auction.usedauction.web.controller;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.repository.sseEmitter.SseEmitterRepository;
import com.auction.usedauction.repository.sseEmitter.SseType;
import com.auction.usedauction.service.SseEmitterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@Slf4j
@RequestMapping("/api/sse")
@RequiredArgsConstructor
//@Tag(name = "Sse 컨트롤러", description = "Sse 관련 api")
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final SseEmitterRepository sseEmitterRepository;

    @GetMapping(value = "/bid-connect/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connectBid(@PathVariable Long productId) {
        Long timeout = 1000 * 60 * 5L; //5분
        String id = sseEmitterService.connect(SseType.BID, productId, timeout);
        SseEmitterDTO findEmitter = sseEmitterRepository.findByEmitterId(id);
        return ResponseEntity.ok(findEmitter.getSseEmitter());
    }

    @GetMapping(value = "/change/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Void> changeBid(@PathVariable Long productId) {

        sseEmitterService.sendUpdatedBidPriceByProductIdTest(productId, 10000);
//        SseEmitterDTO findEmitter = sseEmitterRepository.findByEmitterId(id);
        return ResponseEntity.ok().build();
    }

}
