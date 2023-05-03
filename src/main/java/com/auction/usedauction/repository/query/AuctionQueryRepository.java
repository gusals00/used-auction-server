package com.auction.usedauction.repository.query;

import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.repository.dto.AuctionIdAndAuctionEndDateDTO;
import com.auction.usedauction.repository.dto.QAuctionIdAndAuctionEndDateDTO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.auction.usedauction.domain.QAuction.*;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 시간 내의 입찰 상태인 auctionId, auctionEndDate 리턴
    public List<AuctionIdAndAuctionEndDateDTO> findIdAndEndDateByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .select(new QAuctionIdAndAuctionEndDateDTO(auction.id, auction.auctionEndDate))
                .from(auction)
                .where(auction.status.eq(AuctionStatus.BID), auction.auctionEndDate.between(startDate, endDate))
                .fetch();
    }

    private BooleanExpression auctionStatusEq(AuctionStatus auctionStatus) {
        return auctionStatus != null ? auction.status.eq(auctionStatus) : null;
    }

    private BooleanExpression afterThanAuctionEndDate(LocalDateTime localDateTime) {
        return localDateTime != null ? auction.auctionEndDate.before(localDateTime) : null;
    }
}
