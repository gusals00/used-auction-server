package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.ChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
    boolean existsByProductId(Long productId);

    @EntityGraph(attributePaths = "member")
    Optional<ChatRoom> findById(Long roomId);

    @Query( value = "select c.sellerLastLeftAt from ChatRoom c where c.id = :roomId")
    LocalDateTime findSellerLastLeftAt(@Param("roomId") Long roomId);

    @Query(value = "select c.buyerLastLeftAt from ChatRoom c where c.id = :roomId")
    LocalDateTime findBuyerLastLeftAt(@Param("roomId") Long roomId);
}
