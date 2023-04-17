package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.config.QueryDslConfig;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.AuthorityRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = QueryDslConfig.class)
class AuctionHistoryRepositoryImplTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionHistoryRepository auctionHistoryRepository;

    @Test
    @DisplayName("최근 입찰자 loginId 조회")
    void getLatestLoginId() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category findCategory2 = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(now.plusDays(1), 10000, 2000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory2, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction2, 14000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 18000, buyer2);
        AuctionHistory auctionHistory3 = createAuctionHistory(auction2, 22000, buyer1);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2, auctionHistory3));
        //when
        Optional<String> latestBidMemberLoginId1 = auctionHistoryRepository.findLatestBidMemberLoginId(auction1.getId());
        String latestBidMemberLoginId2 = auctionHistoryRepository.findLatestBidMemberLoginId(auction2.getId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        //then
        assertThat(latestBidMemberLoginId1.isEmpty()).isTrue();
        assertThat(latestBidMemberLoginId2).isEqualTo(buyer1.getLoginId());

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

    private Authority createAuthority(String name) {
        return Authority.builder()
                .authorityName(name)
                .build();
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
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

    private Member createMember(String name, String birth, String email, String loginId, String password, String phoneNumber, Authority authorities) {
        return Member.builder()
                .name(name)
                .birth(birth)
                .email(email)
                .loginId(loginId)
                .password(password)
                .phoneNumber(phoneNumber)
                .authorities(Collections.singleton(authorities))
                .build();
    }

}