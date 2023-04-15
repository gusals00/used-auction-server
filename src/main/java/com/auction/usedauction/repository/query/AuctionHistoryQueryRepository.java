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
import java.util.Optional;

import static com.auction.usedauction.domain.QAuction.auction;
import static com.auction.usedauction.domain.QAuctionHistory.auctionHistory;
import static com.auction.usedauction.domain.QCategory.category;
import static com.auction.usedauction.domain.QMember.member;
import static com.auction.usedauction.domain.QProduct.product;

@Repository
@RequiredArgsConstructor
public class AuctionHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<MyPageAuctionHistoryPageContentRes> findMyAuctionHistoryByCond(String loginId, MyPageSearchConReq cond, Pageable pageable) {
        List<MyPageAuctionHistoryPageContentRes> contents = queryFactory
                .select(new QMyPageAuctionHistoryPageContentRes(product.id, category.name, product.name, auctionHistory.bidPrice, auctionHistory.createdDate, auctionHistory.status))
                .from(auctionHistory)
                .join(auctionHistory.auction, auction)
                .join(auctionHistory.member, member)
                .join(auction.product, product)
                .join(product.category, category)
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

//    public Optional<BiddingHistoryCheckDTO> findLatestAuctionHistoryByAuctionId(Long auctionId) {
//        QMember seller = new QMember("seller");
//        return Optional.ofNullable(
//                queryFactory
//                        .select(new QBiddingHistoryCheckDTO(auctionHistory.id, auction.id, member.loginId,auctionHistory.bidPrice,auction.startPrice,auction.priceUnit,seller.loginId))
//                        .from(auctionHistory)
//                        .join(auctionHistory.member, member)
//                        .rightJoin(auctionHistory.auction, auction)
//                        .join(auction.product,product)
//                        .join(product.member, seller)
//                        .where(auctionIdEq(auctionId),auctionStatusEq(AuctionStatus.BID))
//                        .orderBy(auctionHistory.bidPrice.desc())
//                        .fetchOne()
//        );
//    }

    public Optional<AuctionHistory> findLatestAuctionHistoryByAuctionId(Long auctionId) {
        QMember seller = new QMember("seller");
        return Optional.ofNullable(
                queryFactory
                        .select(auctionHistory)
                        .from(auctionHistory)
                        .join(auctionHistory.member, member)
                        .rightJoin(auctionHistory.auction, auction)
                        .join(auction.product,product)
                        .join(product.member, seller)
                        .where(auctionIdEq(auctionId),auctionStatusEq(AuctionStatus.BID),productStatusEq(ProductStatus.EXIST))
                        .orderBy(auctionHistory.bidPrice.desc())
                        .fetchOne()
        );
    }

    private BooleanExpression statusEq(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        } else if (status.equals("successful-bid")) {
            return auctionHistory.status.eq(AuctionHistoryStatus.SUCCESSFUL_BID);
        } else {
            return auctionHistory.status.eq(AuctionHistoryStatus.BID);
        }
    }

    private BooleanExpression loginIdEq(String loginId) {
        return loginId != null ? auctionHistory.member.loginId.eq(loginId) : null;
    }

    private BooleanExpression auctionIdEq(Long auctionId) {
        return auctionId != null ?auction.id.eq(auctionId) : null;
    }
    private BooleanExpression auctionStatusEq(AuctionStatus auctionStatus) {
        return auctionStatus != null ? auction.status.eq(auctionStatus) : null;
    }

    private BooleanExpression productStatusEq(ProductStatus productStatus) {
        return productStatus != null ? product.productStatus.eq(productStatus) : null;
    }
}
