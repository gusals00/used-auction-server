package com.auction.usedauction.repository.query;

import com.auction.usedauction.repository.dto.ChatRoomCreateInfoDTO;
import com.auction.usedauction.repository.dto.QChatRoomCreateInfoDTO;
import com.auction.usedauction.repository.dto.QSellerAndBuyerLoginIdDTO;
import com.auction.usedauction.repository.dto.SellerAndBuyerLoginIdDTO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.auction.usedauction.domain.QChatRoom.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<SellerAndBuyerLoginIdDTO> findJoinedMembers(Long roomId) {
        return Optional.of(
                queryFactory.select(new QSellerAndBuyerLoginIdDTO(product.member.loginId, chatRoom.member.loginId))
                        .from(chatRoom)
                        .join(chatRoom.member, member)
                        .join(chatRoom.product, product)
                        .join(product.member, member)
                        .where(roomIdEq(roomId))
                        .fetchOne()
        );
    }

    public Optional<ChatRoomCreateInfoDTO> findRoomInfo(Long roomId) {
        return Optional.of(
                queryFactory.select(new QChatRoomCreateInfoDTO(chatRoom.id, product.name))
                        .from(chatRoom)
                        .join(chatRoom.product, product)
                        .where(roomIdEq(roomId))
                        .fetchOne()
        );
    }

    private BooleanExpression roomIdEq(Long roomId) {
        return roomId != null ? chatRoom.id.eq(roomId) : null;
    }
}
