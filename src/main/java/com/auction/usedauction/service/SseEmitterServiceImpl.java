package com.auction.usedauction.service;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.repository.sseEmitter.SseEmitterRepository;
import com.auction.usedauction.repository.sseEmitter.SseSendName;
import com.auction.usedauction.repository.sseEmitter.SseType;
import com.auction.usedauction.service.dto.SseDataRes;
import com.auction.usedauction.service.dto.SseSendDTO;
import com.auction.usedauction.service.dto.SseUpdatePriceDTO;
import com.auction.usedauction.util.SseEmitterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

    private final SseEmitterRepository emitterRepository;

    @Override
    public String connect(SseType sseType, String loginId, Long productId, Long timeout) {
        String sseEmitterId = SseEmitterUtils.createSseEmitterId(sseType, loginId, productId);
        connect(sseEmitterId, timeout);
        return sseEmitterId;
    }

    @Override
    public String connect(SseType sseType, Long productId, Long timeout) {
        String sseEmitterId = SseEmitterUtils.createSseEmitterId(sseType, productId);
        connect(sseEmitterId, timeout);
        return sseEmitterId;
    }

    @Override
    public String connect(SseType sseType, String loginId, Long timeout) {
        String sseEmitterId = SseEmitterUtils.createSseEmitterId(sseType, loginId);
        connect(sseEmitterId, timeout);
        return sseEmitterId;
    }

    private void connect(String sseEmitterId, Long timeout) {
        SseEmitterDTO sseEmitterDTO = emitterRepository.saveEmitter(sseEmitterId, createSseEmitter(timeout));
        onCompleteAndTimeout(sseEmitterDTO);
        send(sseEmitterDTO, SseSendName.CONNECT, "연결 성공!");
    }

    @Override
    public void send(SseSendDTO sseSendDTO) {
        send(sseSendDTO.getSseEmitterDTO(), sseSendDTO.getName(), sseSendDTO.getData());
    }

    private void send(SseEmitterDTO sseEmitterDTO, SseSendName name, Object data) {
        try {
            sseEmitterDTO.getSseEmitter()
                    .send(SseEmitter.event()
                            .id(sseEmitterDTO.getSseEmitterId())
                            .name(name.toString())
                            .data(new SseDataRes<>(sseEmitterDTO.getSseEmitterId(), data)));
            log.info("sse emitter send. emitterId = {}, name = {}", sseEmitterDTO.getSseEmitterId(), name);
        } catch (IOException e) {
            log.error("sse emitter send error. emitterId = {}, name = {}", sseEmitterDTO.getSseEmitterId(), name);
        }
    }

    @Override
    @Async
    public void sendUpdatedBidPrice(SseUpdatePriceDTO updatePriceDTO) {
        sendUpdatedBidPrice(updatePriceDTO.getProductId(), updatePriceDTO.getPrice());
    }

    private void sendUpdatedBidPrice(Long productId, int price) {
        List<SseEmitterDTO> findEmitterList = emitterRepository.findAllByTypeAndProductId(SseType.BID, productId);
        findEmitterList.forEach(
                sseEmitterDTO -> {
                    send(sseEmitterDTO, SseSendName.SEND_BID_DATA, price);
                }
        );
    }

    private void onCompleteAndTimeout(SseEmitterDTO sseEmitterDTO) {
        sseEmitterDTO.getSseEmitter().onCompletion(() -> {
            log.info("onCompletion callback. sseEmitterId = {}", sseEmitterDTO.getSseEmitterId());
            emitterRepository.deleteByEmitterId(sseEmitterDTO.getSseEmitterId()); // 만료되면 emitterMap 에서 삭제
        });

        // timeout일 경우
        sseEmitterDTO.getSseEmitter().onTimeout(() -> {
            log.info("onTimeout callback. sseEmitterId = {}", sseEmitterDTO.getSseEmitterId());
            sseEmitterDTO.getSseEmitter().complete();
        });
    }

    private SseEmitter createSseEmitter(Long timeout) {
        if (timeout != null && timeout > 0) {
            return new SseEmitter(timeout);
        } else {
            return new SseEmitter();
        }
    }
}
