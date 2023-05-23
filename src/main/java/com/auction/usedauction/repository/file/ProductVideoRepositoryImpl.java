package com.auction.usedauction.repository.file;

import com.auction.usedauction.domain.QMember;
import com.auction.usedauction.domain.QProduct;
import com.auction.usedauction.domain.file.ProductVideo;
import com.auction.usedauction.domain.file.QProductVideo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.auction.usedauction.domain.QMember.*;
import static com.auction.usedauction.domain.QProduct.*;
import static com.auction.usedauction.domain.file.QProductVideo.*;

@RequiredArgsConstructor

public class ProductVideoRepositoryImpl implements ProductVideoRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductVideo> findByProductVideoFetch(Long videoId) {
        return Optional.ofNullable(queryFactory.selectFrom(productVideo)
                .join(productVideo.product, product)
                .join(product.member, member)
                .where(productVideo.id.eq(videoId))
                .fetchOne());
    }
}
