package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.config.QueryDslConfig;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = QueryDslConfig.class)
@Transactional
class AuctionHistoryRepositoryCustomImplTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuctionHistoryRepository auctionHistoryRepository;

    @Test
    @DisplayName("낙찰 상태로 변경할 경매별 경매내역 id 리스트 조회")
    void auctionIdAndCount() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer3 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 50000, 10000);
        Auction auction2 = createAuction(now.plusDays(7), 10000, 1000);
        Auction auction3 = createAuction(now.plusDays(1), 10000, 1000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);
        Product product3 = createProduct("상품3", "상품3입니다", seller, findCategory1, auction3);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2, auction3));
        productRepository.saveAll(Arrays.asList(product1, product2, product3));

        //auction 1 입찰
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 60000, buyer2);
        auctionHistoryRepository.saveAll(Arrays.asList(createAuctionHistory(auction1, 50000, buyer1),auctionHistory1));
        //auction 2 입찰
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 30000, buyer1);
        auctionHistoryRepository.saveAll(Arrays.asList(createAuctionHistory(auction2, 20000, buyer2), auctionHistory2));
        //auction 3 입찰
        AuctionHistory auctionHistory3 = createAuctionHistory(auction3, 40000, buyer3);
        auctionHistoryRepository.saveAll(Arrays.asList(createAuctionHistory(auction3, 20000, buyer2), createAuctionHistory(auction3, 30000, buyer1), auctionHistory3));
        auction3.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        //when
        List<Long> result = auctionHistoryRepository.findAuctionHistoryIdForChangeStatus(Arrays.asList(auction1.getId(), auction2.getId(), auction3.getId()));

        //then
        assertThat(result).containsExactlyInAnyOrder(auctionHistory1.getId(),auctionHistory2.getId(),auctionHistory3.getId());
    }


    private AuctionHistory createAuctionHistory(Auction auction, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .auction(auction)
                .bidPrice(bidPrice)
                .member(member)
                .build();
    }

    private Auction createAuction(LocalDateTime endDate, int startPrice, int priceUnit) {
        return Auction.builder()
                .auctionEndDate(endDate)
                .startPrice(startPrice)
                .priceUnit(priceUnit)
                .build();
    }

    private Product createProduct(String productName, String info, Member member, Category category, Auction auction) {
        return Product.builder()
                .info(info)
                .category(category)
                .member(member)
                .name(productName)
                .auction(auction)
                .build();
    }
}