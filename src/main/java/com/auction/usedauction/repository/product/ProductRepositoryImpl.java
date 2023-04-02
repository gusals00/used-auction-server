package com.auction.usedauction.repository.product;

import com.auction.usedauction.domain.MemberStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.ProductStatus;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.repository.dto.ProductOrderCond;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

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
                .join(product.category, category).fetchJoin()
                .join(product.member, member).fetchJoin()
                .orderBy(orderCond(searchCond.getOrderBy()))
                .where(productNameContains(searchCond.getProductName()),
                        categoryIdEq(searchCond.getCategoryId()),
                        productStatusEq(ProductStatus.BID),
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
                        productStatusEq(ProductStatus.BID),
                        memberStatusEq(MemberStatus.EXIST)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

    }

    //상품 상세 조회
    @Override
    public Optional<Product> findProductInfoById(Long productId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(product)
                .join(product.category, category).fetchJoin()
                .join(product.member, member).fetchJoin()
                .where(memberStatusEq(MemberStatus.EXIST),
                        productIdEq(productId),
                        productStatusNotEq(ProductStatus.DELETED)
                )
                .fetchOne());
    }


    private OrderSpecifier orderCond(ProductOrderCond orderCond) {
        if (orderCond == ProductOrderCond.VIEW_ORDER) {
            return product.viewCount.desc();
        } else if (orderCond == ProductOrderCond.NEW_PRODUCT_ORDER) {
            return product.createdDate.desc();
        } else if (orderCond == ProductOrderCond.BID_CLOSING_ORDER) {
            return product.auctionEndDate.asc();
        } else if (orderCond == ProductOrderCond.HIGH_PRICE_ORDER) {
            return product.nowPrice.desc();
        } else {
            return product.nowPrice.asc();
        }
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
}
