package com.auction.usedauction.repository.query;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.repository.dto.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.auction.usedauction.domain.QAuction.auction;
import static com.auction.usedauction.domain.QAuctionHistory.auctionHistory;
import static com.auction.usedauction.domain.QProduct.product;

@Repository
@RequiredArgsConstructor
public class AuctionHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 판매자와 구매자 id 찾기
    public Optional<SellerAndBuyerIdDTO> findSellerAndBuyerId(Long auctionId) {
        QMember buyer = new QMember("buyer");
        QMember seller = new QMember("seller");

        return Optional.ofNullable(
                queryFactory.select(new QSellerAndBuyerIdDTO(seller.id, buyer.id))
                        .from(auctionHistory)
                        .join(auctionHistory.member, buyer)
                        .rightJoin(auctionHistory.auction, auction)
                        .join(auction.product, product)
                        .join(product.member, seller)
                        .where(auctionIdEq(auctionId))
                        .orderBy(auctionHistory.bidPrice.desc())
                        .fetchFirst()
        );
    }

    // 판매자와 구매자 loginId 찾기
    public Optional<SellerAndBuyerLoginIdDTO> findSellerAndBuyerLoginId(Long auctionId) {
        QMember buyer = new QMember("buyer");
        QMember seller = new QMember("seller");

        return Optional.ofNullable(
                queryFactory.select(new QSellerAndBuyerLoginIdDTO(seller.loginId, buyer.loginId))
                        .from(auctionHistory)
                        .join(auctionHistory.member, buyer)
                        .rightJoin(auctionHistory.auction, auction)
                        .join(auction.product, product)
                        .join(product.member, seller)
                        .where(auctionIdEq(auctionId))
                        .orderBy(auctionHistory.bidPrice.desc())
                        .fetchFirst()
        );
    }

    // auctionId, 판매자와 구매자 loginId 리스트 찾기
    public List<AuctionIdAndLoginIds> findSellerAndBuyerLoginIdAndAuctionId(List auctionIds) {
        QMember buyer = new QMember("buyer");
        QMember seller = new QMember("seller");

        return
                queryFactory.select(new QAuctionIdAndLoginIds(product.id, seller.loginId, buyer.loginId))
                        .from(auctionHistory)
                        .join(auctionHistory.member, buyer)
                        .rightJoin(auctionHistory.auction, auction)
                        .join(auction.product, product)
                        .join(product.member, seller)
                        .where(auctionIdIn(auctionIds),
                                auction.nowPrice.eq(auctionHistory.bidPrice))
                        .fetch();

    }

    private BooleanExpression auctionIdEq(Long auctionId) {
        return auctionId != null ? auction.id.eq(auctionId) : null;
    }

    private BooleanExpression auctionIdIn(List auctionIds) {
        return auctionIds != null ? auction.id.in(auctionIds) : null;
    }
}
