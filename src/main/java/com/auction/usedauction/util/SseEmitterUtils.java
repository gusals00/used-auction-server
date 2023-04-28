package com.auction.usedauction.util;

import com.auction.usedauction.repository.sseEmitter.SseType;

import java.util.UUID;

public class SseEmitterUtils {

    // sseType-UUID-productId( 로그인 아이디를 모르는 경우)
    public static String createSseEmitterId(SseType sseType, Long productId) {
        return new StringBuilder()
                .append(sseType.toString())
                .append("-")
                .append(UUID.randomUUID().toString())
                .append("-")
                .append(productId)
                .toString();
    }

    // sseType-loginId-productId(로그인 아이디를 아는 경우)
    public static String createSseEmitterId(SseType sseType, String loginId, Long productId) {
        return new StringBuilder()
                .append(sseType.toString())
                .append("-")
                .append(loginId)
                .append("-")
                .append(productId)
                .toString();
    }

    public static String getSseEmitterSubId(SseType sseType, String loginId) {
        return new StringBuilder()
                .append(sseType.toString())
                .append("-")
                .append(loginId)
                .append("-")
                .toString();
    }
}
