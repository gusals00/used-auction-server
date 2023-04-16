package com.auction.usedauction.repository.auction_history;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.auction.usedauction.domain.QAuction.*;
import static com.auction.usedauction.domain.QAuctionHistory.*;
import static com.auction.usedauction.domain.QMember.*;

@RequiredArgsConstructor
public class AuctionHistoryRepositoryImpl implements AuctionHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<String> findLatestBidMemberLoginId(Long auctionId) {
        return Optional.ofNullable(
                queryFactory.select(member.loginId)
                        .from(auctionHistory)
                        .join(auctionHistory.member, member)
                        .join(auctionHistory.auction, auction)
                        .where(auctionIdEq(auctionId))
                        .orderBy(auctionHistory.bidPrice.desc())
                        .fetchFirst()
        );
    }

    private BooleanExpression auctionIdEq(Long auctionId) {
        return auctionId != null ?auction.id.eq(auctionId) : null;
    }


}
