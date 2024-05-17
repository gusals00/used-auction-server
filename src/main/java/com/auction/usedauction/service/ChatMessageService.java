package com.auction.usedauction.service;

import com.auction.usedauction.domain.Chat;
import com.auction.usedauction.kafka.KafkaMessageDTO;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.chat.ChatRepository;
import com.auction.usedauction.service.sseEmitter.SseEmitterService;
import com.auction.usedauction.util.RedisUtil;
import com.auction.usedauction.web.dto.ChatMessageDTO;
import com.auction.usedauction.web.dto.ChatMessageRes;
import com.auction.usedauction.web.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.auction.usedauction.util.RedisConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatMessageService {

    private final ChatRoomService chatRoomService;
    private final KafkaTemplate<String, KafkaMessageDTO> kafkaTemplate;
    private final SimpMessageSendingOperations template;
    private static final String TOPIC = "chat";
    private final ChatRepository chatRepository;
    private final RedisTemplate<String, MessageDTO> redisTemplate;
    private final SseEmitterService sseEmitterService;

    public List<ChatMessageRes> getChatMessage(Long roomId, LocalDateTime lastTime, int pageSize) {
        if (lastTime == null) {
            lastTime = LocalDateTime.now();
        }

        String cacheKey = MESSAGE + "_" + roomId;
        Set<MessageDTO> cachedMessage = redisTemplate.opsForZSet().reverseRangeByScore(cacheKey, Double.NEGATIVE_INFINITY, lastTime.toEpochSecond(ZoneOffset.of("+9")), 0, pageSize);

        if(cachedMessage.isEmpty() || cachedMessage.size() < pageSize) {
            List<Chat> additionalChat = chatRepository.findByChatRoomIdAndCreatedDateLessThanEqualOrderByCreatedDateDesc(roomId, lastTime, PageRequest.of(0, pageSize - cachedMessage.size()));

            additionalChat.forEach(chat -> redisTemplate.opsForZSet().add(cacheKey, new MessageDTO(roomId, chat.getSender(), chat.getMessage(), chat.getCreatedDate(), UUID.randomUUID().toString()), chat.getCreatedDate().toEpochSecond(ZoneOffset.of("+9"))));
            cachedMessage.addAll(additionalChat.stream().map(chat -> new MessageDTO(roomId, chat.getSender(), chat.getMessage(), chat.getCreatedDate(), UUID.randomUUID().toString())).collect(Collectors.toList()));
        }

        return cachedMessage.stream().map(ChatMessageRes::new).collect(Collectors.toList());
    }

    public void send(String topic, ChatMessageDTO messageDTO) {
        LocalDateTime now = LocalDateTime.now();

        String cacheKey = MESSAGE + "_" + messageDTO.getChatRoomId();
        redisTemplate.opsForZSet().add(cacheKey, new MessageDTO(messageDTO.getChatRoomId(), messageDTO.getSender(), messageDTO.getMessage(), now, UUID.randomUUID().toString()), now.toEpochSecond(ZoneOffset.of("+9")));
        redisTemplate.expire(cacheKey, Duration.ofHours(3));

        Chat chat = convertToChat(messageDTO, now);
        chatRepository.insert(chat);

        boolean isRead = chatRoomService.getUserCount(messageDTO.getChatRoomId()) == 2 ? true : false;
        KafkaMessageDTO kafkaMessageDTO = convertToKafkaMessageDTO(messageDTO, now, isRead);

        kafkaTemplate.send(topic, kafkaMessageDTO);
    }

    @KafkaListener(topics = TOPIC, groupId = ConsumerConfig.GROUP_ID_CONFIG)
    public void consume(KafkaMessageDTO messageDTO) {
        template.convertAndSend("/sub/room/" + messageDTO.getChatRoomId(), messageDTO);
    }

    private KafkaMessageDTO convertToKafkaMessageDTO(ChatMessageDTO messageDTO, LocalDateTime now, boolean isRead) {
        return KafkaMessageDTO.builder()
                .message(messageDTO.getMessage())
                .chatRoomId(messageDTO.getChatRoomId())
                .sender(messageDTO.getSender())
                .isRead(isRead)
                .createdDate(now)
                .build();
    }

    private Chat convertToChat(ChatMessageDTO messageDTO, LocalDateTime now) {
        return Chat.builder()
                .message(messageDTO.getMessage())
                .sender(messageDTO.getSender())
                .chatRoomId(messageDTO.getChatRoomId())
                .createdDate(now)
                .build();
    }
}
