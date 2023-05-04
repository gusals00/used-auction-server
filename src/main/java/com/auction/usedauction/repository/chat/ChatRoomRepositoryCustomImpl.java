package com.auction.usedauction.repository.chat;

import com.auction.usedauction.domain.ChatRoom;
import com.auction.usedauction.domain.QChatMessage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.auction.usedauction.domain.QChatMessage.*;
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

    @Override
    public List<ChatRoom> findChatRoomsByMemberLoginId(String loginId) {
        return queryFactory
                .selectFrom(chatRoom)
                .join(chatRoom.member, member)
                .join(chatRoom.product, product)
                .where(chatRoom.member.loginId.eq(loginId).or(product.member.loginId.eq(loginId)))
                .fetch();
    }

    @Override
    public boolean existsUnReadMessages(Long roomId, String loginId) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(chatMessage)
                .join(chatMessage.member, member)
                .where(chatMessage.chatRoom.id.eq(roomId),
                        chatMessage.readOrNot.eq(false),
                        chatMessage.member.loginId.ne(loginId))
                .fetchFirst();

        return fetchOne != null;
    }
}
