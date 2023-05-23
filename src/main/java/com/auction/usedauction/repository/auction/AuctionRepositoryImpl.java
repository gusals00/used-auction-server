package com.auction.usedauction.repository.auction;

import com.auction.usedauction.domain.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.auction.usedauction.domain.QAuction.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;

@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Auction> findBidAuctionByAuctionIdWithFetchJoin(Long auctionId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(auction)
                        .join(auction.product, product).fetchJoin()
                        .join(product.member, member).fetchJoin()
                        .where(auctionIdEq(auctionId), auctionStatusEq(AuctionStatus.BID))
//                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne());
    }

    @Override
    public List<Long> findSuccessButNotTransIdByDate(LocalDateTime date) {
        return queryFactory.select(auction.id)
                .from(auction)
                .where(auctionStatusEq(AuctionStatus.SUCCESS_BID), auction.auctionEndDate.after(date))
                .fetch();
    }

    @Override
    public Optional<Auction> findAuctionByProductId(Long productId) {
        return Optional.ofNullable(
                queryFactory.select(auction)
                        .from(auction)
                        .join(auction.product, product)
                        .where(productIdEq(productId)).fetchOne());
    }

    private BooleanExpression auctionIdEq(Long auctionId) {
        return auctionId != null ? auction.id.eq(auctionId) : null;
    }

    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? product.id.eq(productId) : null;
    }

    private BooleanExpression auctionStatusEq(AuctionStatus auctionStatus) {
        return auctionStatus != null ? auction.status.eq(auctionStatus) : null;
    }
}
