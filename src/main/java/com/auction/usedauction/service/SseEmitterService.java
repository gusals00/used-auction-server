package com.auction.usedauction.service;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.repository.sseEmitter.SseSendName;
import com.auction.usedauction.repository.sseEmitter.SseType;

public interface SseEmitterService {

    String connect(SseType sseType, String loginId, Long productId, Long timeout);

    String connect(SseType sseType, Long productId, Long timeout);

    void send(SseEmitterDTO sseEmitterDTO, SseSendName name, Object data);

    void sendUpdatedBidPriceByProductIdTest(Long productId,int price);
}
