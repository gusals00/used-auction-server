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
    @Autowired
    private AuctionService auctionService;

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
        Auction auction2 = createAuction(now.plusDays(7), 40000, 5000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        //when
        //auction1 입찰
        auctionHistoryService.biddingAuction(auction1.getId(), 50000, buyer1.getLoginId());
        //auction2 입찰
        auctionHistoryService.biddingAuction(auction2.getId(), 50000, buyer1.getLoginId());
        auctionHistoryService.biddingAuction(auction2.getId(), 55000, buyer2.getLoginId());
        auctionHistoryService.biddingAuction(auction2.getId(), 65000, buyer3.getLoginId());
        auctionHistoryService.biddingAuction(auction2.getId(), 70000, buyer2.getLoginId());

        //then
        // 연속 2번 입찰이 불가능한 경우
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction1.getId(), 58000, buyer1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NOT_BID_BUYER.getMessage());

        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction2.getId(), 90000, buyer2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.NOT_BID_BUYER.getMessage());
    }

    @Test
    @DisplayName("입찰 실패, 입찰가가 너무 큰 경우(입찰가 > 현재가 * 2인 경우)")
    void bidFail4() throws Exception {

        //given
        LocalDateTime now = LocalDateTime.now();
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction2 = createAuction(now.plusDays(7), 40000, 5000);

        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);

        auctionRepository.save(auction2);
        productRepository.save(product2);

        //when
        //auction2 입찰
        auctionHistoryService.biddingAuction(auction2.getId(), 50000, buyer1.getLoginId());

        //then
        //입찰가가 너무 큰 경우 (현재 가격 : 50,000 입찰가 : 100,001)
        assertThatThrownBy(() -> auctionHistoryService.biddingAuction(auction2.getId(), 100001, buyer2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.HIGHER_THAN_MAX_PRICE.getMessage());
    }

    @Test
    @DisplayName("경매 종료시 경매 상태, 경매내역 상태 변경(scheduler)")
    void changeAuctionAndHistoryStatus() throws Exception {
        //given
        LocalDateTime time = LocalDateTime.now().withHour(1);
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //상품 및 경매 생성
        Auction auction1 = createAuction(time.plusMinutes(1), 50000, 10000);
        Auction auction2 = createAuction(time.plusMinutes(2), 10000, 1000);
        Auction auction3 = createAuction(time.plusMinutes(3), 10000, 1000);

        Product product1 = createProduct("상품1", "상품1입니다", seller, findCategory1, auction1);
        Product product2 = createProduct("상품2", "상품2입니다", seller, findCategory1, auction2);
        Product product3 = createProduct("상품3", "상품3입니다", seller, findCategory1, auction3);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2, auction3));
        productRepository.saveAll(Arrays.asList(product1, product2, product3));

        //auction 1 입찰
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 50000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction1, 60000, buyer2);
        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2));
        //auction 2 입찰
        AuctionHistory auctionHistory3 = createAuctionHistory(auction2, 20000, buyer2);
        AuctionHistory auctionHistory4 = createAuctionHistory(auction2, 30000, buyer1);
        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory3, auctionHistory4));

        //when
        // 경매 종료 시간 이후 scheduler 로직 실행
        auctionHistoryService.changeAuctionStatusToAuctionEndStatuses(time.withHour(23).withMinute(59));

        //then
        List<Auction> successBidAuctionList = auctionRepository.findAllById(Arrays.asList(auction1.getId(), auction2.getId()));
        Auction failBidAuction = auctionRepository.findById(auction3.getId()).orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));
        //경매 상태
        //낙찰성공
        assertThat(successBidAuctionList).extracting("status").containsOnly(AuctionStatus.SUCCESS_BID);
        //낙찰실패
        assertThat(failBidAuction.getStatus()).isEqualTo(AuctionStatus.FAIL_BID);

        //경매 기록 상태
        List<AuctionHistory> successBidHistoryList = auctionHistoryRepository.findAllById(Arrays.asList(auctionHistory2.getId(), auctionHistory4.getId()));
        List<AuctionHistory> bidHistoryList = auctionHistoryRepository.findAllById(Arrays.asList(auctionHistory1.getId(), auctionHistory3.getId()));
        // 낙찰 상태 경매 기록
        assertThat(successBidHistoryList).extracting("status").containsOnly(AuctionHistoryStatus.SUCCESSFUL_BID);
        // 입찰 상태 경매 기록
        assertThat(bidHistoryList).extracting("status").containsOnly(AuctionHistoryStatus.BID);


    }

    @Test
    @DisplayName("회원 ban 성공")
    void ban() throws Exception {
        //given
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);
        Auction auction3 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);


        Product product1 = createProduct("상품 거래1", "상품 거래 입니다1", seller, findCategory1, auction1);
        Product product2 = createProduct("상품 거래2", "상품 거래 입니다2", seller, findCategory1, auction2);
        Product product3 = createProduct("상품 거래3", "상품 거래 입니다3", seller, findCategory1, auction3);


        auctionRepository.saveAll(Arrays.asList(auction1, auction2, auction3));
        productRepository.saveAll(Arrays.asList(product1, product2, product3));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 15000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 21000, buyer2);
        AuctionHistory auctionHistory3 = createAuctionHistory(auction3, 21000, buyer1);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2, auctionHistory3));
        // 경매에서 낙찰 상태로 변경
        auction1.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction2.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction3.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        // 입찰 내역에서 낙찰 상태로 변경
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory2.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory3.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);

        //auction1 거래 확정
        auctionService.memberTransConfirm(auction1.getId(), buyer1.getLoginId(), TransStatus.TRANS_REJECT);
        auctionService.memberTransConfirm(auction1.getId(), seller.getLoginId(), TransStatus.TRANS_COMPLETE);

        //auction2 거래 확정
        auctionService.memberTransConfirm(auction2.getId(), buyer2.getLoginId(), TransStatus.TRANS_REJECT);
        auctionService.memberTransConfirm(auction2.getId(), seller.getLoginId(), TransStatus.TRANS_REJECT);

        //auction3 거래 확정
        auctionService.memberTransConfirm(auction3.getId(), buyer1.getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(auction3.getId(), seller.getLoginId(), TransStatus.TRANS_REJECT);

        auctionHistoryService.banMemberByAuctionId(auction1.getId());
        auctionHistoryService.banMemberByAuctionId(auction2.getId());
        auctionHistoryService.banMemberByAuctionId(auction3.getId());

        Member findMember1 = memberRepository.findById(seller.getId()).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        assertThat(findMember1.getStatus()).isEqualTo(MemberStatus.DELETED);
        Member findMember2 = memberRepository.findById(buyer1.getId()).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        assertThat(findMember2.getStatus()).isEqualTo(MemberStatus.EXIST);
        Member findMember3 = memberRepository.findById(buyer2.getId()).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        assertThat(findMember3.getStatus()).isEqualTo(MemberStatus.EXIST);
    }

    private Auction createAuction(LocalDateTime endDate, int startPrice, int priceUnit) {
        return Auction.builder()
                .auctionEndDate(endDate)
                .startPrice(startPrice)
                .priceUnit(priceUnit)
                .build();
    }

    private AuctionHistory createAuctionHistory(Auction auction, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .auction(auction)
                .bidPrice(bidPrice)
                .member(member)
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