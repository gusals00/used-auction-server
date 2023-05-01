package com.auction.usedauction.service;

import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.repository.dto.ChatRoomCreateInfoDTO;
import com.auction.usedauction.repository.dto.SellerAndBuyerLoginIdDTO;
import com.auction.usedauction.repository.dto.SseEmitterDTO;
import com.auction.usedauction.repository.query.ChatRoomQueryRepository;
import com.auction.usedauction.repository.sseEmitter.SseEmitterRepository;
import com.auction.usedauction.repository.sseEmitter.SseSendName;
import com.auction.usedauction.repository.sseEmitter.SseType;
import com.auction.usedauction.service.dto.SseDataRes;
import com.auction.usedauction.service.dto.SseRoomEnterDataRes;
import com.auction.usedauction.service.dto.SseSendDTO;
import com.auction.usedauction.service.dto.SseUpdatePriceDTO;
import com.auction.usedauction.util.RedisUtil;
import com.auction.usedauction.util.SseEmitterUtils;
import com.auction.usedauction.web.dto.ChatMessageDTO;
import com.auction.usedauction.web.dto.SseRoomDataRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static com.auction.usedauction.exception.error_code.ChatErrorCode.*;
import static com.auction.usedauction.repository.sseEmitter.SseSendName.*;
import static com.auction.usedauction.repository.sseEmitter.SseType.CHAT_LIST;
import static com.auction.usedauction.util.RedisConstants.ROOM_LIST;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

    private final SseEmitterRepository emitterRepository;
    private final ChatRoomService chatRoomService;
    private final RedisUtil redisUtil;
    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomRepository chatRoomRepository;

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
        send(sseEmitterDTO, CONNECT, "연결 성공!");
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
            emitterRepository.deleteByEmitterId(sseEmitterDTO.getSseEmitterId());
            log.info("sse delete. emitterId = {}, name = {}", sseEmitterDTO.getSseEmitterId(), name);

        }
    }

    @Override
    @Async
    public void sendUpdatedBidPrice(SseUpdatePriceDTO updatePriceDTO) {
        sendUpdatedBidPrice(updatePriceDTO.getProductId(), updatePriceDTO.getPrice());
    }

    @Override
    public void sendUpdatedRoomData(ChatMessageDTO messageDTO, String senderLoginId, boolean isRead) {
        List<SseEmitterDTO> emitterList = emitterRepository.findAllByType(CHAT_LIST);

        emitterList.forEach(sseEmitterDTO -> {
            String loginId = sseEmitterDTO.getSseEmitterId().split("-")[1]; // emitterId 에서 loginId 꺼냄
            List<String> joinedRoomList = redisUtil.getList(ROOM_LIST + loginId); // redis 에서 loginId의 입장중인 방 리스트 꺼냄

            if(joinedRoomList == null) {
                joinedRoomList = chatRoomService.addJoinedRoomListToRedis(loginId); // 방 리스트가 만료되었으면 다시 저장함
            }

            if(joinedRoomList.contains(messageDTO.getChatRoomId().toString())) { // 내가 입장중인 방에 온 메세지일 때
                boolean unReadMessage = true;
                if(!isRead) { // 읽음처리 돼서 가는 메세지가 아닐 때
                    if(!loginId.equals(senderLoginId)) { // 내가 보낸 메세지가 아닐 때
                        unReadMessage = false;
                    }
                }
                // 채팅방 데이터 전송
                send(new SseSendDTO(sseEmitterDTO, SEND_ROOM_DATA, new SseRoomDataRes(messageDTO.getChatRoomId(), messageDTO.getMessage(), messageDTO.getSender(), unReadMessage)));
            }
        });
    }

    @Override
    public void sendNewRoomData(Long roomId) {
        List<SseEmitterDTO> emitterList = emitterRepository.findAllByType(CHAT_LIST);
        SellerAndBuyerLoginIdDTO sellerAndBuyerLoginIdDTO = chatRoomQueryRepository.findJoinedMembers(roomId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        emitterList.forEach(sseEmitterDTO -> {
            String loginId = sseEmitterDTO.getSseEmitterId().split("-")[1]; // emitterId 에서 loginId 꺼냄

            // sse 연결된 사용자의 방이 생성되었을 때
            if(loginId.equals(sellerAndBuyerLoginIdDTO.getSellerLoginId()) || loginId.equals(sellerAndBuyerLoginIdDTO.getBuyerLoginId())) {
                ChatRoomCreateInfoDTO chatRoomCreateInfoDTO = chatRoomQueryRepository.findRoomInfo(roomId)
                        .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

                // 생성된 채팅방 정보 전송
                send(sseEmitterDTO, SEND_NEW_ROOM_DATA, chatRoomCreateInfoDTO);
            }
        });
    }

    @Override
    public void sendRoomEnterData(Long roomId, String loginId) {
        if(chatRoomRepository.existsUnReadMessages(roomId, loginId)) { // 입장한 채팅방에 안읽은 메세지가 존재할 때
            List<SseEmitterDTO> emitterList = emitterRepository.findAllByTypeAndLoginId(CHAT_LIST, loginId);

            emitterList.forEach(sseEmitterDTO -> send(sseEmitterDTO, SEND_ROOM_ENTER_DATA, new SseRoomEnterDataRes(roomId))); // 채팅방 정보 전송
        }
    }

    private void sendUpdatedBidPrice(Long productId, int price) {
        List<SseEmitterDTO> findEmitterList = emitterRepository.findAllByTypeAndProductId(SseType.BID, productId);
        findEmitterList.forEach(
                sseEmitterDTO -> {
                    send(sseEmitterDTO, SEND_BID_DATA, price);
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
