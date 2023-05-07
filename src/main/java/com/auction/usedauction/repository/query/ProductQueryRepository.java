package com.auction.usedauction.repository.query;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.QAuction;
import com.auction.usedauction.domain.QMember;
import com.auction.usedauction.domain.QProduct;
import com.auction.usedauction.repository.dto.QTransactionCountDTO;
import com.auction.usedauction.repository.dto.TransactionCountDTO;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.auction.usedauction.domain.QAuction.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public TransactionCountDTO findTransactionCountByLoginId(String loginId) {
        return queryFactory
                .select(new QTransactionCountDTO(
                        auction.count(),
                        new CaseBuilder()
                                .when(auction.status.eq(AuctionStatus.TRANSACTION_OK))
                                .then(1L)
                                .otherwise(0L)
                                .sum())
                )
                .from(product)
                .join(product.member, member)
                .join(product.auction, auction)
                .where(product.member.loginId.eq(loginId),
                        auction.status.eq(AuctionStatus.TRANSACTION_FAIL).or(auction.status.eq(AuctionStatus.TRANSACTION_OK)))
                .fetchOne();
    }
}
