package com.auction.usedauction.repository.query;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.QAuction;
import com.auction.usedauction.domain.QMember;
import com.auction.usedauction.domain.QProduct;
import com.auction.usedauction.domain.file.ProductImageType;
import com.auction.usedauction.domain.file.QFile;
import com.auction.usedauction.domain.file.QProductImage;
import com.auction.usedauction.repository.dto.ProductInfoDTO;
import com.auction.usedauction.repository.dto.QProductInfoDTO;
import com.auction.usedauction.repository.dto.QTransactionCountDTO;
import com.auction.usedauction.repository.dto.TransactionCountDTO;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.auction.usedauction.domain.QAuction.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;
import static com.auction.usedauction.domain.file.QFile.*;
import static com.auction.usedauction.domain.file.QProductImage.*;

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

    public ProductInfoDTO findSuccessBidProductInfoById(Long productId) {
        return queryFactory
                .select(new QProductInfoDTO(product.id, productImage.fullPath, auction.nowPrice))
                .from(productImage)
                .join(productImage.product, product)
                .join(product.auction, auction)
                .where(product.id.eq(productId),
                        productImage.type.eq(ProductImageType.SIGNATURE))
                .fetchOne();
    }
}
