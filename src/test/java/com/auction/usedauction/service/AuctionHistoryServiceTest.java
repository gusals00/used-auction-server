package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.AuctionHistoryErrorCode;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class AuctionHistoryServiceTest {

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
    @Autowired
    private AuctionHistoryService auctionHistoryService;

    @Test
    @DisplayName("입찰 성공")
    void bidSuccess() throws Exception {
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

        //when
        AuctionBidResultDTO auctionResult1 = auctionHistoryService.biddingAuction(auction1.getId(), 20000, buyer1.getLoginId());
        AuctionBidResultDTO auctionResult2 = auctionHistoryService.biddingAuction(auction1.getId(), 26000, buyer2.getLoginId());
        AuctionBidResultDTO auctionResult3 = auctionHistoryService.biddingAuction(auction2.getId(), 10000, buyer2.getLoginId());

        //then
        // auction1 테스트
        AuctionHistory findHistory1 = auctionHistoryRepository.findById(auctionResult1.getAuctionHistoryId())
                .orElseThrow(() -> new CustomException(AuctionHistoryErrorCode.AUCTION_HISTORY_NOT_FOUND));
        AuctionHistory findHistory2 = auctionHistoryRepository.findById(auctionResult2.getAuctionHistoryId())
                .orElseThrow(() -> new CustomException(AuctionHistoryErrorCode.AUCTION_HISTORY_NOT_FOUND));

        assertThat(findHistory1.getId()).isEqualTo(auctionResult1.getAuctionHistoryId());
        assertThat(findHistory1.getBidPrice()).isEqualTo(auctionResult1.getNowPrice());
        assertThat(findHistory1.getMember().getLoginId()).isEqualTo(buyer1.getLoginId());

        assertThat(findHistory2.getId()).isEqualTo(auctionResult2.getAuctionHistoryId());
        assertThat(findHistory2.getBidPrice()).isEqualTo(auctionResult2.getNowPrice());
        assertThat(findHistory2.getMember().getLoginId()).isEqualTo(buyer2.getLoginId());

        // auction1 현재가 비교
        Auction findAuction1 = auctionRepository.findById(auction1.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));
        assertThat(findAuction1.getNowPrice()).isEqualTo(auction1.getNowPrice());

        // auction2 테스트
        AuctionHistory findHistory3 = auctionHistoryRepository.findById(auctionResult3.getAuctionHistoryId())
                .orElseThrow(() -> new CustomException(AuctionHistoryErrorCode.AUCTION_HISTORY_NOT_FOUND));
        assertThat(findHistory3.getId()).isEqualTo(auctionResult3.getAuctionHistoryId());
        assertThat(findHistory3.getBidPrice()).isEqualTo(auctionResult3.getNowPrice());
        assertThat(findHistory3.getMember().getLoginId()).isEqualTo(buyer2.getLoginId());

        // auction2 현재가 비교(첫 입찰일 경우 시작가로 입찰 가능)
        Auction findAuction2 = auctionRepository.findById(auction2.getId())
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));
        assertThat(findAuction2.getNowPrice()).isEqualTo(auction2.getStartPrice());


    }

    @Test
    @DisplayName("입찰 실패, 판매자는 입찰 불가능/존재하지 않는 회원일 경우")
    void bidFail1() throws Exception {

        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        String notExistMemberLoginId = "1234dfgere";
        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 14000, 1000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);

        auctionRepository.save(auction1);
        productRepository.save(product1);

        //then
        // 판매자는 입찰 불가능
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 20000, seller.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NOT_BID_SELLER.getMessage());
        // 존재하지 않는 회원일 경우
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 26000, notExistMemberLoginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());


    }

    @Test
    @DisplayName("입찰 실패, 입찰가가 올바르지 않은 경우")
    void bidFail2() throws Exception {

        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 50000, 4000);
        Auction auction2 = createAuction(now.plusDays(7), 10000, 4000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));


        //when
        auctionHistoryService.biddingAuction(auction1.getId(), 50000, buyer1.getLoginId());

        //then
        // 입찰 단위가 맞지 않는 경우
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 55000, buyer2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.INVALID_PRICE_UNIT.getMessage());

        // 첫 입찰자가 아닌 경우, 현재가보다 반드시 높은 가격이어야 함
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 50000, buyer2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NO_HIGHER_THAN_NOW_PRICE.getMessage());
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 49999, buyer2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NO_HIGHER_THAN_NOW_PRICE.getMessage());

        // 첫 입찰자인 경우, 현재가보다 같거나 큰 가격이어야 함
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction2.getId(), 9999, buyer1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NO_HIGHER_THAN_NOW_PRICE.getMessage());
        // 첫 입찰자일 때 현재가==입찰가 가능
        auctionHistoryService.biddingAuction(auction2.getId(), 10000, buyer1.getLoginId());

    }

    @Test
    @DisplayName("입찰 실패, 최근 입찰자와 현재 입찰자가 다르지 않은 경우")
    void bidFail3() throws Exception {

        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer3 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(now.plusDays(4), 50000, 4000);
        Auction auction2 = createAuction(now.plusDays(7), 10000, 4000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        auctionRepository.save(auction1);
        productRepository.save(product1);

        //when
        //auction1 입찰
        auctionHistoryService.biddingAuction(auction1.getId(), 50000, buyer1.getLoginId());
        //auction2 입찰
        auctionHistoryService.biddingAuction(auction2.getId(), 50000, buyer1.getLoginId());
        auctionHistoryService.biddingAuction(auction2.getId(), 54000, buyer2.getLoginId());
        auctionHistoryService.biddingAuction(auction2.getId(), 62000, buyer3.getLoginId());
        auctionHistoryService.biddingAuction(auction2.getId(), 66000, buyer2.getLoginId());


        //then
        // 연속 2번 입찰이 불가능한 경우
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 58000, buyer1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NOT_BID_BUYER.getMessage());

        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction2.getId(), 70000, buyer2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NOT_BID_BUYER.getMessage());
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