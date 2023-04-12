package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.AuctionHistoryStatus;
import com.auction.usedauction.domain.QCategory;
import com.auction.usedauction.domain.QProduct;
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

import static com.auction.usedauction.domain.QAuctionHistory.*;
import static com.auction.usedauction.domain.QCategory.*;
import static com.auction.usedauction.domain.QProduct.*;

@RequiredArgsConstructor
public class AuctionHistoryRepositoryCustomImpl implements AuctionHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    //마이페이지 입찰,낙찰 내역 조회
    @Override
    public Page<AuctionHistory> findMyAuctionHistoryByCond(String loginId, MyPageSearchConReq cond, Pageable pageable) {
        List<AuctionHistory> content = queryFactory
                .selectFrom(auctionHistory)
                .join(auctionHistory.product, product).fetchJoin()
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
                .where(
                        statusEq(cond.getStatus()),
                        loginIdEq(loginId)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(String status) {
        if(!StringUtils.hasText(status)) {
            return null;
        } else if(status.equals("successful-bid")) {
            return auctionHistory.status.eq(AuctionHistoryStatus.SUCCESSFUL_BID);
        } else {
            return auctionHistory.status.eq(AuctionHistoryStatus.BID);
        }
    }

    private BooleanExpression loginIdEq(String loginId) {
        return loginId != null ? auctionHistory.member.loginId.eq(loginId) : null;
    }

}
