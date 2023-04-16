package com.auction.usedauction.repository.query;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.repository.dto.MyPageAuctionHistoryPageContentRes;
import com.auction.usedauction.repository.dto.QMyPageAuctionHistoryPageContentRes;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.auction.usedauction.domain.QAuction.auction;
import static com.auction.usedauction.domain.QAuctionHistory.auctionHistory;
import static com.auction.usedauction.domain.QCategory.category;
import static com.auction.usedauction.domain.QMember.member;
import static com.auction.usedauction.domain.QProduct.product;

@Repository
@RequiredArgsConstructor
public class AuctionHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    private BooleanExpression auctionIdEq(Long auctionId) {
        return auctionId != null ?auction.id.eq(auctionId) : null;
    }
    private BooleanExpression auctionStatusEq(AuctionStatus auctionStatus) {
        return auctionStatus != null ? auctionHistory.auction.status.eq(auctionStatus) : null;
    }

}
