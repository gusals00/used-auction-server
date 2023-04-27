package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.ChatRoom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.auction.usedauction.domain.QChatRoom.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;

@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatRoom> findChatRoomsByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(chatRoom)
                .join(chatRoom.member, member)
                .join(chatRoom.product, product).fetchJoin()
                .where(chatRoom.member.id.eq(memberId).or(product.member.id.eq(memberId)))
                .fetch();
    }
}
