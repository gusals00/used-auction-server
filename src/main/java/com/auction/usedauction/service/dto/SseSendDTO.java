package com.auction.usedauction.service.dto;

import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.repository.sseEmitter.SseSendName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SseSendDTO {
    private SseEmitterDTO sseEmitterDTO;
    private SseSendName name;
    private Object data;
}
