package com.auction.usedauction;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile(value = {"test"})
public class InitTestService{

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;
    private final CategoryRepository categoryRepository;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void initTest() {
//        Authority authority = createAuthority("ROLE_USER");
//        authorityRepository.save(authority);
//
//        Member member1 = createMember("호창", "990428", "addd@naver.com", "20180584", "1234", "010-5444-8888", authority);
//        Member member2 = createMember("광민", "990228", "addd333@naver.com", "20180012", "133234", "010-5944-8288", authority);
//        Member member3 = createMember("시철", "990118", "addsdfd333@naver.com", "20180592", "1332234", "010-4944-8288", authority);
//        Member member4 = createMember("대현", "990228", "addd333@naver.com", "20180004", "1323234", "010-5944-8288", authority);
//        Member member5 = createMember("성수", "990118", "addsdfd333@naver.com", "20180211", "13322334", "010-4244-8288", authority);
//
//
//        memberRepository.saveAll(Arrays.asList(member1, member2, member3,member4,member5));
//
//        List<Category> categoryList = new ArrayList<>(Arrays.asList(
//                createCategory("디지털기기"), createCategory("생활가전"), createCategory("가구/인테리어"),
//                createCategory("생활/주방"), createCategory("유아동"), createCategory("유아도서"),
//                createCategory("여성의류"), createCategory("여성잡화"), createCategory("도서"),
//                createCategory("가공식품"), createCategory("반려동물용품"), createCategory("식품"),
//                createCategory("기타"), createCategory("남성패션/잡화"), createCategory("뷰티/미용"),
//                createCategory("티켓/교환권"), createCategory("스포츠/레저"), createCategory("취미/게임/음반")
//        ));
//        categoryRepository.saveAll(categoryList);
//
//
//        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
//        Category findCategory2 = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
//
//        //상품 및 경매 생성
//        Auction auction1 = createAuction(LocalDateTime.now().plusDays(4), 14000, 1000);
//        Auction auction2 = createAuction(LocalDateTime.now().plusDays(1), 10000, 2000);
//
//        Product product1 = createProduct("상품1", "상품1입니다", member1, findCategory1, auction1);
//        Product product2 = createProduct("상품2", "상품2입니다", member1, findCategory2, auction2);
//
//        auctionRepository.saveAll(Arrays.asList(auction1, auction2));
//        productRepository.saveAll(Arrays.asList(product1, product2));
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