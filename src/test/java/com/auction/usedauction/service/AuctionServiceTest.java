package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class AuctionServiceTest {

    @Autowired
    private AuctionService auctionService;
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
    @DisplayName("판매자 거래 확정(거래완료, 거래불발) 성공")
    void transConfirm1() throws Exception {
        //given
        Member seller1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member seller2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Member buyer1 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);

        Product product1 = createProduct("상품 거래1", "상품 거래 입니다1", seller1, findCategory1, auction1);
        Product product2 = createProduct("상품 거래2", "상품 거래 입니다2", seller2, findCategory1, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 15000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 21000, buyer1);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2));
        // 경매에서 낙찰 상태로 변경
        auction1.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction2.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        // 입찰 내역에서 낙찰 상태로 변경
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory2.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);

        //when
        // 판매자 TransStatus 거래 완료/ 거래 불발로 변경
        auctionService.memberTransConfirm(auction1.getId(), seller1.getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(auction2.getId(), seller2.getLoginId(), TransStatus.TRANS_REJECT);

        //then
        //거래 완료
        assertThat(auction1.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_COMPLETE);
        assertThat(auction1.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_BEFORE);
        assertThat(auction1.getStatus()).isEqualTo(AuctionStatus.SUCCESS_BID);
        //거래 불발
        assertThat(auction2.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_REJECT);
        assertThat(auction2.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_BEFORE);
        assertThat(auction2.getStatus()).isEqualTo(AuctionStatus.SUCCESS_BID);
    }

    @Test
    @DisplayName("구매자 거래 확정(거래완료, 거래불발) 성공")
    void transConfirm2() throws Exception {
        //given
        Member seller1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);

        Product product1 = createProduct("상품 거래1", "상품 거래 입니다1", seller1, findCategory1, auction1);
        Product product2 = createProduct("상품 거래2", "상품 거래 입니다2", seller1, findCategory1, auction2);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 15000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 21000, buyer2);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2));
        // 경매에서 낙찰 상태로 변경
        auction1.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction2.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        // 입찰 내역에서 낙찰 상태로 변경
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory2.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);

        //when
        // 판매자 TransStatus 거래 완료/ 거래 불발로 변경
        auctionService.memberTransConfirm(auction1.getId(), buyer1.getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(auction2.getId(), buyer2.getLoginId(), TransStatus.TRANS_REJECT);

        //then
        //거래 완료
        assertThat(auction1.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_BEFORE);
        assertThat(auction1.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_COMPLETE);
        assertThat(auction1.getStatus()).isEqualTo(AuctionStatus.SUCCESS_BID);
        //거래 불발
        assertThat(auction2.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_BEFORE);
        assertThat(auction2.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_REJECT);
        assertThat(auction2.getStatus()).isEqualTo(AuctionStatus.SUCCESS_BID);
    }

    @Test
    @DisplayName("구매자,판매자 거래 확정 성공, 경매 상태 변경(거래 성공/거래 실패)")
    void transConfirm3() throws Exception {
        //given
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer3 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer4 = memberRepository.findByLoginId("20180004").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);
        Auction auction3 = createAuction(LocalDateTime.now().plusDays(3), 30000, 1000);
        Auction auction4 = createAuction(LocalDateTime.now().plusDays(3), 40000, 1000);

        Product product1 = createProduct("상품 거래1", "상품 거래 입니다1", seller, findCategory1, auction1);
        Product product2 = createProduct("상품 거래2", "상품 거래 입니다2", seller, findCategory1, auction2);
        Product product3 = createProduct("상품 거래3", "상품 거래 입니다3", seller, findCategory1, auction3);
        Product product4 = createProduct("상품 거래4", "상품 거래 입니다4", seller, findCategory1, auction4);

        auctionRepository.saveAll(Arrays.asList(auction1, auction2, auction3, auction4));
        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 15000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 21000, buyer2);
        AuctionHistory auctionHistory3 = createAuctionHistory(auction3, 31000, buyer3);
        AuctionHistory auctionHistory4 = createAuctionHistory(auction4, 41000, buyer4);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2, auctionHistory3, auctionHistory4));
        // 경매에서 낙찰 상태로 변경
        auction1.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction2.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction3.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction4.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        // 입찰 내역에서 낙찰 상태로 변경
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory2.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory3.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory4.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);

        //when
        // 판매자 -> 거래 완료, 구매자 -> 거래 완료
        auctionService.memberTransConfirm(auction1.getId(), seller.getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(auction1.getId(), buyer1.getLoginId(), TransStatus.TRANS_COMPLETE);
        // 판매자 -> 거래 완료, 구매자 -> 거래 불발
        auctionService.memberTransConfirm(auction2.getId(), seller.getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(auction2.getId(), buyer2.getLoginId(), TransStatus.TRANS_REJECT);
        // 판매자 -> 거래 불발, 구매자 -> 거래 완료
        auctionService.memberTransConfirm(auction3.getId(), seller.getLoginId(), TransStatus.TRANS_REJECT);
        auctionService.memberTransConfirm(auction3.getId(), buyer3.getLoginId(), TransStatus.TRANS_COMPLETE);
        // 판매자 -> 거래 불발, 구매자 -> 거래 불발
        auctionService.memberTransConfirm(auction4.getId(), seller.getLoginId(), TransStatus.TRANS_REJECT);
        auctionService.memberTransConfirm(auction4.getId(), buyer4.getLoginId(), TransStatus.TRANS_REJECT);

        //then
        // 판매자 -> 거래 완료, 구매자 -> 거래 완료
        assertThat(auction1.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_COMPLETE);
        assertThat(auction1.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_COMPLETE);
        assertThat(auction1.getStatus()).isEqualTo(AuctionStatus.TRANSACTION_OK);
        // 판매자 -> 거래 완료, 구매자 -> 거래 불발
        assertThat(auction2.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_COMPLETE);
        assertThat(auction2.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_REJECT);
        assertThat(auction2.getStatus()).isEqualTo(AuctionStatus.TRANSACTION_FAIL);

        // 판매자 -> 거래 불발, 구매자 -> 거래 완료
        assertThat(auction3.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_REJECT);
        assertThat(auction3.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_COMPLETE);
        assertThat(auction3.getStatus()).isEqualTo(AuctionStatus.TRANSACTION_FAIL);

        // 판매자 -> 거래 불발, 구매자 -> 거래 불발
        assertThat(auction4.getSellerTransStatus()).isEqualTo(TransStatus.TRANS_REJECT);
        assertThat(auction4.getBuyerTransStatus()).isEqualTo(TransStatus.TRANS_REJECT);
        assertThat(auction4.getStatus()).isEqualTo(AuctionStatus.TRANSACTION_FAIL);
    }

    @Test
    @DisplayName("회원 거래 확정 실패, 낙찰 성공 경매 존재X/올바른 회원이 아닌경우(구매자,판매자가 아닌 경우) ")
    void transConfirmFail1() throws Exception {
        //given
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member invalidMember = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);


        Product product1 = createProduct("상품 거래1", "상품 거래 입니다1", seller, findCategory1, auction1);
        Product product2 = createProduct("상품 거래2", "상품 거래 입니다2", seller, findCategory1, auction2);


        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
        productRepository.saveAll(Arrays.asList(product1, product2));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 15000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 21000, buyer2);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2));
        // 경매에서 낙찰 상태로 변경
        auction2.changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        // 입찰 내역에서 낙찰 상태로 변경
        auctionHistory2.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);


        //then
        // 낙찰 성공 경매 존재X
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction1.getId(), seller.getLoginId(), TransStatus.TRANS_COMPLETE))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.AUCTION_NOT_FOUND.getMessage());
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction1.getId(), buyer1.getLoginId(), TransStatus.TRANS_COMPLETE))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.AUCTION_NOT_FOUND.getMessage());

        // 올바른 회원이 아닌경우(구매자,판매자가 아닌 경우)
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction1.getId(), invalidMember.getLoginId(), TransStatus.TRANS_COMPLETE))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.AUCTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 거래 확정 실패, 변경하려는 상태 파라미터가 올바르지 않은 경우/이미 거래 확정이 종료된 경매인 경우")
    void transConfirmFail2() throws Exception {
        //given
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer1 = memberRepository.findByLoginId("20180211").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member invalidMember = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
        Auction auction2 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);
        Auction auction3 = createAuction(LocalDateTime.now().plusDays(3), 20000, 1000);


        Product product1 = createProduct("상품 거래1", "상품 거래 입니다1", seller, findCategory1, auction1);
        Product product2 = createProduct("상품 거래2", "상품 거래 입니다2", seller, findCategory1, auction2);
        Product product3 = createProduct("상품 거래3", "상품 거래 입니다3", seller, findCategory1, auction3);


        auctionRepository.saveAll(Arrays.asList(auction1, auction2,auction3));
        productRepository.saveAll(Arrays.asList(product1, product2,product3));

        // 입찰 내역 추가
        AuctionHistory auctionHistory1 = createAuctionHistory(auction1, 15000, buyer1);
        AuctionHistory auctionHistory2 = createAuctionHistory(auction2, 21000, buyer2);
        AuctionHistory auctionHistory3 = createAuctionHistory(auction3, 21000, buyer1);

        auctionHistoryRepository.saveAll(Arrays.asList(auctionHistory1, auctionHistory2,auctionHistory3));
        // 경매에서 낙찰 상태로 변경
        auction1.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction2.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auction3.changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        // 입찰 내역에서 낙찰 상태로 변경
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory2.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        auctionHistory3.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);

        //auction2 구매자가 거래 확정
        auctionService.memberTransConfirm(auction2.getId(), buyer2.getLoginId(), TransStatus.TRANS_REJECT);

        //auction3 구매자,판매자가 거래 확정
        auctionService.memberTransConfirm(auction3.getId(), buyer1.getLoginId(), TransStatus.TRANS_REJECT);
        auctionService.memberTransConfirm(auction3.getId(),seller.getLoginId(), TransStatus.TRANS_REJECT);

        //then
        //변경하려는 상태가 올바르지 않은 경우(파라미터 = TRANS_BEFORE)
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction1.getId(), seller.getLoginId(), TransStatus.TRANS_BEFORE))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_CHANGE_TRANS.getMessage());
        //구매자가 이미 거래 확정을 한 경우
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction2.getId(), buyer2.getLoginId(), TransStatus.TRANS_REJECT))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.ALREADY_USER_CHANGE_TRANS.getMessage());

        //auction3 구매자, 판매자가 거래 확정을 하여 거래 확정이 모두 종료된 경우
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction3.getId(), buyer1.getLoginId(), TransStatus.TRANS_REJECT))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.ALREADY_AUCTION_TRANS_COMPLETE.getMessage());
        assertThatThrownBy(() ->  auctionService.memberTransConfirm(auction3.getId(), seller.getLoginId(), TransStatus.TRANS_COMPLETE))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.ALREADY_AUCTION_TRANS_COMPLETE.getMessage());
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