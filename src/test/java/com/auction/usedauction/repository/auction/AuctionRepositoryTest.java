package com.auction.usedauction.repository.auction;

import com.auction.usedauction.config.DataJpaTestConfig;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.dto.AuctionIdAndBidCountDTO;
import com.auction.usedauction.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = DataJpaTestConfig.class)
@Transactional
class AuctionRepositoryTest {

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
    @DisplayName("현재 입찰 상태인 경매 조회")
    void getProductListPage1() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category findCategory2 = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(now.plusDays(1), 10000, 2000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory2, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        auction1.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        //when
        Auction findAuction2 = auctionRepository.findBidAuctionByAuctionIdWithFetchJoin(auction2.getId()).orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));

        //then
        // 경매 1은 낙찰 상태이기에 예외 발생
        assertThatThrownBy(() -> auctionRepository.findBidAuctionByAuctionIdWithFetchJoin(auction1.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND))
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.AUCTION_NOT_FOUND.getMessage());

        assertThat(findAuction2.getNowPrice()).isEqualTo(auction2.getNowPrice());
        assertThat(findAuction2.getProduct().getMember().getId()).isEqualTo(seller.getId());


    }

    @Test
    @DisplayName("경매 상태 bulk update")
    void auctionStatusBulkUpdate() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 50000, 10000);
        Auction auction2 = createAuction(now.plusDays(7), 10000, 1000);
        Auction auction3 = createAuction(now.plusDays(1), 10000, 1000);
        Auction auction4 = createAuction(now.plusDays(10), 10000, 1000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);
        Product product3 = createProduct("상품3", "상품3입니다", seller, findCategory1, auction3);
        Product product4 = createProduct("상품4", "상품4입니다", seller, findCategory1, auction4);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2, auction3, auction4));
        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4));

        //when
        auctionRepository.updateAuctionStatus(AuctionStatus.SUCCESS_BID,Arrays.asList(auction2.getId(), auction4.getId()));
        auctionRepository.updateAuctionStatus(AuctionStatus.FAIL_BID,Arrays.asList(auction1.getId(), auction3.getId()));

        //then
        List<Auction> success = auctionRepository.findAllById(Arrays.asList(auction2.getId(), auction4.getId()));
        List<Auction> fail = auctionRepository.findAllById(Arrays.asList(auction1.getId(), auction3.getId()));

        assertThat(success).extracting("status").containsOnly(AuctionStatus.SUCCESS_BID);
        assertThat(fail).extracting("status").containsOnly(AuctionStatus.FAIL_BID);
    }

    @Test
    @DisplayName("입찰 종료할 경매 id, 입찰수 조회")
    void auctionIdAndCount() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 50000, 10000);
        Auction auction2 = createAuction(now.plusDays(7), 10000, 1000);
        Auction auction3 = createAuction(now.plusDays(1), 10000, 1000);
        Auction auction4 = createAuction(now.plusDays(10), 10000, 1000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);
        Product product3 = createProduct("상품3", "상품3입니다", seller, findCategory1, auction3);
        Product product4 = createProduct("상품4", "상품4입니다", seller, findCategory1, auction4);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2, auction3, auction4));
        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4));

        auctionHistoryRepository.saveAll(Arrays.asList(createAuctionHistory(auction1, 50000, buyer1), createAuctionHistory(auction1, 60000, buyer2)));
        auction3.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        //when
        List<AuctionIdAndBidCountDTO> result = auctionRepository.findIdAndBidCountListByStatusAndEndDate(AuctionStatus.BID, now.plusDays(9));

        //then
        assertThat(result.size()).isEqualTo(2);
        // 확인을 위해 정렬
        List<AuctionIdAndBidCountDTO> sorted = result.stream()
                .sorted(Comparator.comparing(AuctionIdAndBidCountDTO::getAuctionId)).toList();
        assertThat(result).extracting("auctionId").contains(auction1.getId(), auction2.getId());
        assertThat(sorted).extracting("count").containsExactly(2L, 0L);
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