package com.auction.usedauction.service;

import com.auction.usedauction.repository.sseEmitter.SseType;
import com.auction.usedauction.service.dto.SseSendDTO;
import com.auction.usedauction.service.dto.SseUpdatePriceDTO;
import org.springframework.transaction.event.TransactionalEventListener;

public interface SseEmitterService {

    String connect(SseType sseType, String loginId, Long productId, Long timeout);
    String connect(SseType sseType, Long productId, Long timeout);
    String connect(SseType sseType, String loginId, Long timeout);
    void send(SseSendDTO sseSendDTO);
    @TransactionalEventListener
    void sendUpdatedBidPrice(SseUpdatePriceDTO updatePriceDTO);
}
