package com.auction.usedauction.repository.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Getter
@AllArgsConstructor
@Setter
public class SseEmitterDTO {

    private String sseEmitterId;
    private SseEmitter sseEmitter;
}
