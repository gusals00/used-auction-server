package com.auction.usedauction.service.sseEmitter;

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
import com.auction.usedauction.util.SseEmitterUtils;
import com.auction.usedauction.web.dto.ChatMessageDTO;
import com.auction.usedauction.web.dto.SseRoomDataRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.auction.usedauction.exception.error_code.ChatErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.auction.usedauction.repository.sseEmitter.SseSendName.*;
import static com.auction.usedauction.repository.sseEmitter.SseType.CHAT_LIST;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

    private final SseEmitterRepository emitterRepository;
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

    @Override
    public String connectAndSendCount(SseType sseType, String loginId, Long timeout, Long notificationCount) {
        String sseEmitterId = SseEmitterUtils.createSseEmitterId(sseType, loginId);
        SseEmitterDTO sseEmitterDTO = emitterRepository.saveEmitter(sseEmitterId, createSseEmitter(timeout));
        onCompleteAndTimeout(sseEmitterDTO);
        send(sseEmitterDTO, CONNECT, notificationCount);

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
        } catch (Exception e) {
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
        // 방아이디로 방 인원 조회
        SellerAndBuyerLoginIdDTO sellerAndBuyerLoginIdDTO = chatRoomQueryRepository.findJoinedMembers(messageDTO.getChatRoomId())
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        List<SseEmitterDTO> buyerEmitterList = emitterRepository.findAllByTypeAndLoginId(CHAT_LIST, sellerAndBuyerLoginIdDTO.getBuyerLoginId());
        List<SseEmitterDTO> sellerEmitterList = emitterRepository.findAllByTypeAndLoginId(CHAT_LIST, sellerAndBuyerLoginIdDTO.getSellerLoginId());

        boolean buyerRead, sellerRead;
        if(senderLoginId.equals(sellerAndBuyerLoginIdDTO.getBuyerLoginId())) { // 내가 보낸 메세지인지 확인
            buyerRead = true;
            sellerRead = isRead;
        } else {
            sellerRead = true;
            buyerRead = isRead;
        }

        // 채팅방 리스트에 접속중인 판매자, 구매자에게 채팅방 데이터 전송
        sendRoomData(buyerEmitterList, messageDTO, buyerRead);
        sendRoomData(sellerEmitterList, messageDTO, sellerRead);
    }

    @Override
    public void sendNewRoomData(Long roomId) {
        // 방아이디로 방 인원 조회
        SellerAndBuyerLoginIdDTO sellerAndBuyerLoginIdDTO = chatRoomQueryRepository.findJoinedMembers(roomId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        List<SseEmitterDTO> buyerEmitterList = emitterRepository.findAllByTypeAndLoginId(CHAT_LIST, sellerAndBuyerLoginIdDTO.getBuyerLoginId());
        List<SseEmitterDTO> sellerEmitterList = emitterRepository.findAllByTypeAndLoginId(CHAT_LIST, sellerAndBuyerLoginIdDTO.getSellerLoginId());

        ChatRoomCreateInfoDTO chatRoomCreateInfoDTO = chatRoomQueryRepository.findRoomInfo(roomId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        // 채팅방 리스트에 접속중인 판매자, 구매자에게 새로 생성된 채팅방 데이터 전송
        sendNewRoomData(buyerEmitterList, chatRoomCreateInfoDTO);
        sendNewRoomData(sellerEmitterList, chatRoomCreateInfoDTO);
    }

    @Override
    public void sendRoomEnterData(Long roomId, String loginId) {
        if(chatRoomRepository.existsUnReadMessages(roomId, loginId)) { // 입장한 채팅방에 안읽은 메세지가 존재할 때
            List<SseEmitterDTO> emitterList = emitterRepository.findAllByTypeAndLoginId(CHAT_LIST, loginId);

            emitterList.forEach(sseEmitterDTO -> send(sseEmitterDTO, SEND_ROOM_ENTER_DATA, new SseRoomEnterDataRes(roomId))); // 채팅방 정보 전송
        }
    }

    @Override
    public void sendNotificationData(String loginId, Long value) {
        List<SseEmitterDTO> findEmitterList = emitterRepository.findAllByTypeAndLoginId(SseType.NOTIFICATION, loginId);
        findEmitterList.forEach(sseEmitterDTO -> {
            send(sseEmitterDTO, SEND_NOTIFICATION_DATA, value);
        });
    }

    private void sendNewRoomData(List<SseEmitterDTO> buyerEmitterList, ChatRoomCreateInfoDTO chatRoomCreateInfoDTO) {
        buyerEmitterList.forEach(sseEmitterDTO ->
                send(sseEmitterDTO, SEND_NEW_ROOM_DATA, chatRoomCreateInfoDTO)
        );
    }

    private void sendRoomData(List<SseEmitterDTO> sellerEmitterList, ChatMessageDTO messageDTO, boolean sellerRead) {
        sellerEmitterList.forEach(sseEmitterDTO ->
                send(new SseSendDTO(sseEmitterDTO, SEND_ROOM_DATA, new SseRoomDataRes(messageDTO.getChatRoomId(), messageDTO.getMessage(), messageDTO.getSender(), sellerRead)))
        );
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
