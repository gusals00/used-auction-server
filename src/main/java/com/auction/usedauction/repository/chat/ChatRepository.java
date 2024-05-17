package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.Chat;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByChatRoomIdAndCreatedDateLessThanEqualOrderByCreatedDateDesc(Long chatRoomId, LocalDateTime createdDate, PageRequest pageRequest);
    Optional<Chat> findTopByChatRoomIdOrderByCreatedDateDesc(Long chatRoomId);

    Long countByChatRoomIdAndCreatedDateAfter(Long chatRoomId, LocalDateTime after);
}
