package com.auction.usedauction.repository.product;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.MemberStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.repository.dto.ProductOrderCond;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.auction.usedauction.domain.QAuction.*;
import static com.auction.usedauction.domain.QCategory.*;
import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;
import static org.springframework.util.StringUtils.*;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    //상품 리스트 조회
    @Override
    public Page<Product> findBySearchCond(ProductSearchCondDTO searchCond, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.category, category)
                .join(product.member, member)
                .join(product.auction, auction)
                .orderBy(orderCond(searchCond.getOrderBy()))
                .where(productNameContains(searchCond.getProductName()),
                        categoryIdEq(searchCond.getCategoryId()),
                        productStatusEq(ProductStatus.EXIST),
                        memberStatusEq(MemberStatus.EXIST)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count query
        JPAQuery<Long> countQuery = queryFactory.select(product.count())
                .from(product)
                .join(product.category, category)
                .join(product.member, member)
                .where(productNameContains(searchCond.getProductName()),
                        categoryIdEq(searchCond.getCategoryId()),
                        productStatusEq(ProductStatus.EXIST),
                        memberStatusEq(MemberStatus.EXIST)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

    }

    //상품 상세 조회
    @Override
    public Optional<Product> findExistProductByIdAndExistMember(Long productId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(product)
                .join(product.category, category)
                .join(product.member, member)
                .where(memberStatusEq(MemberStatus.EXIST),
                        productIdEq(productId),
                        productStatusEq(ProductStatus.EXIST)
                )
                .fetchOne());
    }

    // 마이페이지 상품 관리
    @Override
    public Page<Product> findMyProductsByCond(String loginId, MyPageSearchConReq cond, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.category, category).fetchJoin()
                .join(product.member, member)
                .join(product.auction, auction).fetchJoin()
                .where(loginIdEq(loginId),
                        statusEq(cond.getStatus()),
                        productStatusEq(ProductStatus.EXIST)
                )
                .orderBy(product.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .join(product.member, member)
                .where(loginIdEq(loginId),
                        statusEq(cond.getStatus()),
                        productStatusEq(ProductStatus.EXIST)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier[] orderCond(ProductOrderCond orderCond) {

        List<OrderSpecifier> orderByList = new ArrayList<>();
        if (orderCond == ProductOrderCond.VIEW_ORDER) {
            orderByList.add(product.viewCount.desc());
        } else if (orderCond == ProductOrderCond.NEW_PRODUCT_ORDER) {
            orderByList.add(product.createdDate.desc());
        } else if (orderCond == ProductOrderCond.BID_CLOSING_ORDER) {
            orderByList.add(auction.auctionEndDate.asc());
        } else if (orderCond == ProductOrderCond.HIGH_PRICE_ORDER) {
            orderByList.add(auction.nowPrice.desc());
        } else {
            orderByList.add(auction.nowPrice.asc());
        }
        return orderByList.toArray(OrderSpecifier[]::new);
    }

    private BooleanExpression productStatusEq(ProductStatus status) {
        return status != null ? product.productStatus.eq(status) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? category.id.eq(categoryId) : null;
    }

    private BooleanExpression productNameContains(String productName) {
        return hasText(productName) ? product.name.contains(productName) : null;
    }


    private BooleanExpression productStatusNotEq(ProductStatus status) {
        return status != null ? product.productStatus.ne(status) : null;
    }

    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? product.id.eq(productId) : null;
    }

    private BooleanExpression memberStatusEq(MemberStatus memberStatus) {
        return memberStatus != null ? product.member.status.eq(memberStatus) : null;
    }

    private BooleanExpression loginIdEq(String loginId) {
        return loginId != null ? product.member.loginId.eq(loginId) : null;
    }

    private BooleanExpression statusEq(String status) {
        if(!StringUtils.hasText(status)) {
            return null;
        } else if(status.equals("success-bid")) {
            return auction.status.eq(AuctionStatus.SUCCESS_BID);
        } else if(status.equals("fail-bid")) {
            return auction.status.eq(AuctionStatus.FAIL_BID);
        } else if(status.equals("transaction-ok")) {
            return auction.status.eq(AuctionStatus.TRANSACTION_OK);
        } else if(status.equals("transaction-fail")) {
            return auction.status.eq(AuctionStatus.TRANSACTION_FAIL);
        } else {
            return auction.status.eq(AuctionStatus.BID);
        }
    }

}
