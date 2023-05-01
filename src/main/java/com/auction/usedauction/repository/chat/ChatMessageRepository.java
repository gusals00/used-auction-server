package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "update chat_message c join member m on c.member_id = m.member_id set c.read_or_not = true where m.login_id not like :loginId and c.chat_room_id = :roomId and c.read_or_not = false", nativeQuery = true)
    int updateMessages(@Param("loginId") String loginId, @Param("roomId") Long roomId);

    @Query(value = "select c from ChatMessage c join fetch c.member where c.chatRoom.id = :chatRoomId order by c.createdDate desc",
            countQuery = "select count(c) from ChatMessage c where c.chatRoom.id = :chatRoomId")
    Page<ChatMessage> findByChatRoomIdOrderByCreatedDateDesc(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
}
