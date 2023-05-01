package com.auction.usedauction.repository.sseEmitter;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.util.SseEmitterUtils;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.*;

@Repository
@NoArgsConstructor
public class SseEmitterRepositoryImpl implements SseEmitterRepository {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    /*
     key : emitterId, value : emitter

     (로그인 아이디를 모르는 경우)
        key(emitterId) -> sseType-UUID-productId
                       -> BID-seffsd.e2233-3
     (로그인 아이디를 아는 경우)
        key(emitterId) -> sseType-loginId-productId
                       -> BID-hyeonmin-3
    */

    @Override
    public SseEmitterDTO saveEmitter(String emitterId, SseEmitter emitter) {
        emitterMap.put(emitterId, emitter);
        return new SseEmitterDTO(emitterId,emitter);
    }

    @Override
    public List<SseEmitterDTO> findAllByTypeAndProductId(SseType sseType, Long productId) {
        return emitterMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(sseType.toString() + "-") && entry.getKey().endsWith("-" + productId))
                .map(entry -> new SseEmitterDTO(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    @Override
    public List<SseEmitterDTO> findAllByTypeAndLoginId(SseType sseType, String loginId) {
        return emitterMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(SseEmitterUtils.getSseEmitterSubId(sseType, loginId)))
                .map(entry -> new SseEmitterDTO(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    @Override
    public List<SseEmitterDTO> findAllByType(SseType sseType) {
        return emitterMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(sseType.toString() + "-"))
                .map(entry -> new SseEmitterDTO(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    @Override
    public SseEmitterDTO findByEmitterId(String emitterId) {
        return emitterMap.containsKey(emitterId) ? new SseEmitterDTO(emitterId, emitterMap.get(emitterId)) : null;
    }

    @Override
    public String deleteByEmitterId(String emitterId) {
        return emitterMap.remove(emitterId) != null ? emitterId : null;
    }
}
