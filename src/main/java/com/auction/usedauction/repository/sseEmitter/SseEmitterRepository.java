package com.auction.usedauction.repository.sseEmitter;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseEmitterRepository {

    SseEmitterDTO saveEmitter(String emitterId,SseEmitter emitter);
    List<SseEmitterDTO> findAllByTypeAndProductId(SseType sseType, Long productId);
    List<SseEmitterDTO> findAllByTypeAndLoginId(SseType sseType, String loginId);
    SseEmitterDTO findByEmitterId(String emitterId);
    String deleteByEmitterId(String emitterId);
}
