package com.auction.usedauction.repository.sseEmitter;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseEmitterRepository {

    //Emitter 저장
    SseEmitterDTO saveEmitter(String emitterId,SseEmitter emitter);
    //SseType + productId로 EmitterList 조회
    List<SseEmitterDTO> findAllByTypeAndProductId(SseType sseType, Long productId);
    //SseType + loginId로 EmitterList 조회
    List<SseEmitterDTO> findAllByTypeAndLoginId(SseType sseType, String loginId);
    //SseType 으로 EmitterList 조회
    List<SseEmitterDTO> findAllByType(SseType sseType);
    //SseEmitterId로 조회
    SseEmitterDTO findByEmitterId(String emitterId);
    //EmitterId로 Emitter 삭제
    String deleteByEmitterId(String emitterId);
}
