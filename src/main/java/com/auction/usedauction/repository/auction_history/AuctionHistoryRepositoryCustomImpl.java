package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.AuctionHistoryStatus;
import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.QMember;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.auction.usedauction.domain.QAuction.auction;
import static com.auction.usedauction.domain.QAuctionHistory.auctionHistory;
import static com.auction.usedauction.domain.QCategory.category;
import static com.auction.usedauction.domain.QMember.member;
import static com.auction.usedauction.domain.QProduct.product;

@RequiredArgsConstructor
public class AuctionHistoryRepositoryCustomImpl implements AuctionHistoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuctionHistory> findMyAuctionHistoryByCond(String loginId, MyPageSearchConReq cond, Pageable pageable) {
        List<AuctionHistory> contents = queryFactory
                .selectFrom(auctionHistory)
                .join(auctionHistory.auction, auction).fetchJoin()
                .join(auctionHistory.member, member)
                .join(auction.product, product).fetchJoin()
                .join(product.category, category).fetchJoin()
                .where(
                        statusEq(cond.getStatus()),
                        loginIdEq(loginId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(auctionHistory.createdDate.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(auctionHistory.count())
                .from(auctionHistory)
                .join(auctionHistory.member, member)
                .where(
                        statusEq(cond.getStatus()),
                        loginIdEq(loginId)
                );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

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

    // 회원별 거래 실패 횟수 조회
    public Long findRejectCountByMemberId(Long memberId) {
        QMember buyer = new QMember("buyer");
        QMember seller = new QMember("seller");

        return queryFactory.select(auctionHistory.count().longValue())
                .from(auctionHistory)
                .join(auctionHistory.member, buyer)
                .join(auctionHistory.auction, auction)
                .join(auction.product, product)
                .join(product.member, seller)
                .where(sellerOrBuyerIdEq(memberId, buyer, seller), auctionStatusEq(AuctionStatus.TRANSACTION_FAIL))
                .fetchOne();
    }

    private BooleanExpression sellerOrBuyerIdEq(Long memberId, QMember buyer, QMember seller) {
        return memberIdEq(memberId, buyer).or(memberIdEq(memberId, seller));
    }

    private BooleanExpression memberIdEq(Long memberId, QMember member) {
        return memberId != null ? member.id.eq(memberId) : null;
    }

    private BooleanExpression auctionStatusEq(AuctionStatus auctionStatus) {
        return auctionStatus != null ? auctionHistory.auction.status.eq(auctionStatus) : null;
    }

    private BooleanExpression statusEq(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        } else if (status.equals("successful-bid")) {
            return auctionHistory.status.eq(AuctionHistoryStatus.SUCCESSFUL_BID);
        } else if (status.equals("bid")) {
            return auctionHistory.status.eq(AuctionHistoryStatus.BID);
        } else {
            return null;
        }
    }

    private BooleanExpression loginIdEq(String loginId) {
        return loginId != null ? auctionHistory.member.loginId.eq(loginId) : null;
    }

    private BooleanExpression auctionIdEq(Long auctionId) {
        return auctionId != null ?auction.id.eq(auctionId) : null;
    }

}
