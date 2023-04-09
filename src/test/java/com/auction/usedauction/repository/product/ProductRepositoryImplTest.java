package com.auction.usedauction.repository.product;

import com.auction.usedauction.config.QueryDslConfig;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.dto.ProductOrderCond;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.auction.usedauction.repository.dto.ProductOrderCond.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = QueryDslConfig.class)
class ProductRepositoryImplTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void beforeEach() {
        Authority authority = createAuthority("ROLE_USER");
        authorityRepository.save(authority);

        Member member1 = createMember("호창", "990428", "addd@naver.com", "20180584", "1234", "010-5444-8888", authority);
        Member member2 = createMember("광민", "990228", "addd333@naver.com", "20180012", "133234", "010-5944-8288", authority);

        memberRepository.saveAll(Arrays.asList(member1, member2));

        List<Category> categoryList = new ArrayList<>(Arrays.asList(
                createCategory("디지털기기"), createCategory("생활가전"), createCategory("가구/인테리어"),
                createCategory("생활/주방"), createCategory("유아동"), createCategory("유아도서"),
                createCategory("여성의류"), createCategory("여성잡화"), createCategory("도서"),
                createCategory("가공식품"), createCategory("반려동물용품"), createCategory("식품"),
                createCategory("기타"), createCategory("남성패션/잡화"), createCategory("뷰티/미용"),
                createCategory("티켓/교환권"), createCategory("스포츠/레저"), createCategory("취미/게임/음반")
        ));
        categoryRepository.saveAll(categoryList);
    }

    @Test
    @DisplayName("상품 페이징 리스트 조회 (전체 카테고리와 특정 카테고리별, 이름으로 조회)")
    void getProductListPage1() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category findCategory2 = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category findCategory3 = categoryRepository.findCategoryByName("기타").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        Product product1 = createProduct("상품1", "상품1입니다", 1000, 14000, 1000, now.plusDays(4), findMember, findCategory1);
        Product product2 = createProduct("상품2", "상품2입니다", 2000, 100200, 1000, now.plusDays(1), findMember, findCategory1);
        Product product3 = createProduct("상품3", "상품3입니다", 55000, 140000, 1000, now.plusDays(2), findMember, findCategory2);
        Product product4 = createProduct("상품1_4", "상품4입니다", 12000, 150000, 1000, now.plusDays(2), findMember, findCategory3);

        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4));

        //when

        // 전체 조회
        ProductSearchCondDTO searchCond1 = createSearchCond(null, "", NEW_PRODUCT_ORDER);
        PageRequest page1 = PageRequest.of(0, 3);
        Page<Product> findProductPage1 = productRepository.findBySearchCond(searchCond1, page1);

        PageRequest page2 = PageRequest.of(1, 3);
        Page<Product> findProductPage2 = productRepository.findBySearchCond(searchCond1, page2);

        PageRequest page3 = PageRequest.of(2, 3);
        Page<Product> findProductPage3 = productRepository.findBySearchCond(searchCond1, page3);

        // 특정 카테고리 조회
        ProductSearchCondDTO searchCond2 = createSearchCond(findCategory1.getId(), null, NEW_PRODUCT_ORDER);
        PageRequest page4 = PageRequest.of(0, 3);
        Page<Product> findProductPage4 = productRepository.findBySearchCond(searchCond2, page4);

        ProductSearchCondDTO searchCond3 = createSearchCond(findCategory2.getId(), "", NEW_PRODUCT_ORDER);
        PageRequest page5 = PageRequest.of(0, 3);
        Page<Product> findProductPage5 = productRepository.findBySearchCond(searchCond3, page5);

        //이름으로 조회
        ProductSearchCondDTO searchCond4 = createSearchCond(null, "상품1", NEW_PRODUCT_ORDER);
        PageRequest page6 = PageRequest.of(0, 3);
        Page<Product> findProductPage6 = productRepository.findBySearchCond(searchCond4, page6);

        ProductSearchCondDTO searchCond5 = createSearchCond(findCategory3.getId(), "상품1", NEW_PRODUCT_ORDER);
        Page<Product> findProductPage7 = productRepository.findBySearchCond(searchCond5, page6);


        //then

        //전체 조회
        assertThat(findProductPage1.getContent()).extracting("name").containsExactly("상품1_4", "상품3", "상품2");
        assertThat(findProductPage2.getContent()).extracting("name").containsExactly("상품1");
        assertThat(findProductPage3.getContent()).isEmpty();

        // 특정 카테고리 조회
        assertThat(findProductPage4.getContent()).extracting("name").containsExactly("상품2", "상품1");
        assertThat(findProductPage5.getContent()).extracting("name").containsExactly("상품3");

        //이름으로 조회
        assertThat(findProductPage6.getContent()).extracting("name").containsExactly("상품1_4", "상품1");
        assertThat(findProductPage7.getContent()).extracting("name").containsExactly("상품1_4");

    }

    @Test
    @DisplayName("상품 페이징 리스트 정렬 기준별 조회")
    void getProductListPage2() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category findCategory2 = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category findCategory3 = categoryRepository.findCategoryByName("기타").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        Product product1 = createProduct("상품1", "상품1입니다", 1000, 14000, 1000, now.plusHours(1), findMember, findCategory1);
        plusViewCount(product1, 5);
        Product product2 = createProduct("상품2", "상품2입니다", 2000, 100000, 1000, now.plusHours(2), findMember, findCategory1);
        plusViewCount(product2, 4);
        Product product3 = createProduct("상품3", "상품3입니다", 55000, 140000, 1000, now.plusHours(3), findMember, findCategory2);
        plusViewCount(product3, 3);
        Product product4 = createProduct("상품4", "상품4입니다", 12000, 190000, 1000, now.plusHours(6), findMember, findCategory3);
        plusViewCount(product4, 2);
        Product product5 = createProduct("상품5", "상품5입니다", 12000, 180000, 1000, now.plusHours(5), findMember, findCategory3);
        plusViewCount(product5, 7);
        Product product6 = createProduct("상품6", "상품6입니다", 12000, 170000, 1000, now.plusHours(4), findMember, findCategory3);
        plusViewCount(product6, 9);

        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4, product5, product6));

        //when
        PageRequest page = PageRequest.of(0, 10);

        // 경매 마감 임박
        ProductSearchCondDTO searchCond1 = createSearchCond(null, null, BID_CLOSING_ORDER);
        Page<Product> findProductPage1 = productRepository.findBySearchCond(searchCond1, page);
        ProductSearchCondDTO searchCond2 = createSearchCond(findCategory3.getId(), null, BID_CLOSING_ORDER);
        Page<Product> findProductPage2 = productRepository.findBySearchCond(searchCond2, page);

        // 조회순
        ProductSearchCondDTO searchCond3 = createSearchCond(null, null, VIEW_ORDER);
        Page<Product> findProductPage3 = productRepository.findBySearchCond(searchCond3, page);
        ProductSearchCondDTO searchCond4 = createSearchCond(findCategory1.getId(), null, VIEW_ORDER);
        Page<Product> findProductPage4 = productRepository.findBySearchCond(searchCond4, page);

        // 현재 가격 높은순
        ProductSearchCondDTO searchCond5 = createSearchCond(null, null, HIGH_PRICE_ORDER);
        Page<Product> findProductPage5 = productRepository.findBySearchCond(searchCond5, page);
        ProductSearchCondDTO searchCond6 = createSearchCond(findCategory2.getId(), null, HIGH_PRICE_ORDER);
        Page<Product> findProductPage6 = productRepository.findBySearchCond(searchCond6, page);

        // 현재 가격 낮은순
        ProductSearchCondDTO searchCond7 = createSearchCond(null, null, LOW_PRICE_ORDER);
        Page<Product> findProductPage7 = productRepository.findBySearchCond(searchCond7, page);
        ProductSearchCondDTO searchCond8 = createSearchCond(findCategory1.getId(), null, LOW_PRICE_ORDER);
        Page<Product> findProductPage8 = productRepository.findBySearchCond(searchCond8, page);

        //then

        // 경매 마감 임박순
        assertThat(findProductPage1.getContent()).extracting("name").containsExactly("상품1", "상품2", "상품3", "상품6", "상품5", "상품4");
        assertThat(findProductPage2.getContent()).extracting("name").containsExactly("상품6", "상품5", "상품4");

        // 조회순
        assertThat(findProductPage3.getContent()).extracting("name").containsExactly("상품6", "상품5", "상품1", "상품2", "상품3", "상품4");
        assertThat(findProductPage4.getContent()).extracting("name").containsExactly("상품1", "상품2");

        // 현재 가격 높은순
        assertThat(findProductPage5.getContent()).extracting("name").containsExactly("상품4", "상품5", "상품6", "상품3", "상품2", "상품1");
        assertThat(findProductPage6.getContent()).extracting("name").containsExactly("상품3");

        // 현재 가격 낮은순
        assertThat(findProductPage7.getContent()).extracting("name").containsExactly("상품1", "상품2", "상품3", "상품6", "상품5", "상품4");
        assertThat(findProductPage8.getContent()).extracting("name").containsExactly("상품1", "상품2");
    }

    @Test
    @DisplayName("상품 단건 조회(category, member와 페치조인)")
    void getProductDetail() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member findMember1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Product product1 = createProduct("상품1", "상품1입니다", 1000, 14000, 1000, now.plusHours(1), findMember1, findCategory1);
        productRepository.save(product1);

        //when
        Product findProduct = productRepository.findProductByIdWithFetchJoin(product1.getId()).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        //then
        Assertions.assertThat(findProduct.getId()).isEqualTo(product1.getId());
    }

    @Test
    @DisplayName("상품 단건 조회 실패, 삭제된 상품인 경우/판매자가 삭제된 경우 ")
    void getProductDetailFail() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        Member findMember1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member findMember2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Product product1 = createProduct("상품1", "상품1입니다", 1000, 14000, 1000, now.plusHours(1), findMember1, findCategory1);
        Product product2 = createProduct("상품2", "상품2입니다", 2000, 14000, 1000, now.plusHours(1), findMember2, findCategory1);
        productRepository.saveAll(Arrays.asList(product1, product2));

        product1.changeProductStatus(ProductStatus.DELETED);
        findMember2.changeStatus(MemberStatus.DELETED);

        //then
        // 삭제된 상품인 경우
        assertThatThrownBy(() ->  productRepository.findProductByIdWithFetchJoin(product1.getId())
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND))
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());

        // 판매자가 삭제된 경우
        assertThatThrownBy(() ->  productRepository.findProductByIdWithFetchJoin(product2.getId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    private void plusViewCount(Product product, int count) {
        for (int i = 0; i < count; i++) {
            product.increaseViewCount();
        }
    }

    private ProductSearchCondDTO createSearchCond(Long categoryId, String productName, ProductOrderCond orderCond) {
        return new ProductSearchCondDTO(categoryId, productName, orderCond);
    }

    private Product createProduct(String productName, String info, int startPrice, int nowPrice, int priceUnit, LocalDateTime endDate, Member member, Category category) {
        return Product.builder()
                .auctionEndDate(endDate)
                .nowPrice(nowPrice)
                .priceUnit(priceUnit)
                .startPrice(startPrice)
                .info(info)
                .category(category)
                .member(member)
                .name(productName)
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
}