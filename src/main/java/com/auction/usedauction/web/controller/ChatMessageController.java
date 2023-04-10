package com.auction.usedauction.web.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations template;

    @MessageMapping("/hello")
    public void test(TestMessage test) {
        template.convertAndSend("/sub/test", test);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class TestMessage {
        String test;
    }
}
