package com.auction.usedauction.loadTest;

import com.auction.usedauction.domain.Authority;
import com.auction.usedauction.domain.Category;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.loadTest.Dummy;
import com.auction.usedauction.loadTest.DummyRepository;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.security.TokenDTO;
import com.auction.usedauction.service.*;
import com.auction.usedauction.service.dto.AuctionRegisterDTO;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.util.s3.FileSubPath;
import com.auction.usedauction.util.s3.UploadFileDTO;
import com.opencsv.CSVWriter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Profile(value = {"local", "production"})
public class InitLoadTest {

    @Value("${INIT_FILE_PATH}")
    private String filePath;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final EntityManager em;
    private final DummyRepository dummyRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final AuthorityRepository authorityRepository;


    // 입찰 데이터가 꼬이지 않고 올바르게 입찰 되었는지 확인
    public boolean validate() {
        List<Long> auctionIds = dummyRepository.findByDistinctBidAuctionId();
        for (Long auctionId : auctionIds) {
            int auctionHistoryCnt = auctionHistoryRepository.findAuctionHistoryByAuctionId(auctionId);
            int distinctAuctionHistoryCnt = auctionHistoryRepository.findDistinctAuctionHistoryByAuctionId(auctionId);
            // 특정 상품에 대해 동일한 입찰 금액이 중복되는지 확인(동시성 처리 실패한 경우)
            if (auctionHistoryCnt != distinctAuctionHistoryCnt) { // true -> 특정 상품에 대해 동일한 입찰 금액이 중복됨 -> 동시성 처리 실패
                log.info("입찰 금액 중복 여부 확인 결과 실패");
                return false;
            }
        }
        log.info("입찰 금액 중복 여부 확인 결과 성공");


        // 입찰 데이터가 꼬인 경우 -> 특정 입찰자가 입찰한 금액과 동일하게 입찰 되었는지 입찰 기록과 비교
        int totalCount = auctionHistoryRepository.countBidAuctionHistoriesByAuctionIds(auctionIds); // 입찰 기록 개수
        int joinWithDummyCount = auctionHistoryRepository.countBidAuctionHistoriesJoinDummy(); // 입찰 기록과 실제 입찰 호출한 기록을 join 한 개수
        log.info("입찰 기록과 실제 입찰 데이터 비교 결과 : {}", totalCount == joinWithDummyCount ? "성공" : "실패");

        return totalCount == joinWithDummyCount;
        // 입찰 기록 개수 != join한 개수 -> 입찰 데이터가 꼬인경우
        // 입찰 기록 개수 == join한 개수 -> 입찰 데이터가 꼬이지 않고 동시성 처리가 제대로 된 경우
    }

    public List<Category> insertCategory() {
        List<Category> categoryList = new ArrayList<>(Arrays.asList(
                createCategory("디지털기기2"), createCategory("생활가전2"), createCategory("가구/인테리어2"),
                createCategory("생활/주방2"), createCategory("유아동2"), createCategory("유아도서2"),
                createCategory("여성의류2"), createCategory("여성잡화2"), createCategory("도서2"),
                createCategory("가공식품2"), createCategory("반려동물용품2"), createCategory("식품2"),
                createCategory("기타2"), createCategory("남성패션/잡화2"), createCategory("뷰티/미용2"),
                createCategory("티켓/교환권2"), createCategory("스포츠/레저2"), createCategory("취미/게임/음반2")
        ));
        categoryRepository.saveAll(categoryList);
        return categoryList;
    }

    public void insertData(int memberCnt, int productCnt, int loopCnt, List<Category> categoryList, CSVWriter writer) throws IOException {
        // 회원 저장
        Authority authority = createAuthority("ROLE_USER");

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < memberCnt; i++) {
            Member member = createMember("김" + (i + 1) + loopCnt, "990828", "hocsung+" + (i + 1) + "@kumoh.ac.kr", "hyeonmin" + ((loopCnt - 1) * memberCnt + i), "password" + (i + 1), "010-1233-1233", authority);
            memberRepository.save(member);
            members.add(member);
        }

        Member sellerMember = createMember("김0", "990828", "hco2323sdf@kumoh.ac.kr", "hoch" + loopCnt, "password0", "010-1233-1233", authority);
        memberRepository.save(sellerMember);


        // 상품 저장
        LocalDateTime now = LocalDateTime.now();
        List<Product> products = new ArrayList<>();
        int priceUnit = 100;
        for (int i = 0; i < productCnt; i++) {
            Product product = insertProduct("상품" + i + loopCnt, "이사가게 되서 팝니다", categoryList.get(i % categoryList.size()).getId(), now.minusDays(7), now.plusDays(10), 20000 + (i * 100), priceUnit,
                    "11_1.jpg", Arrays.asList("11_2.jpg"), sellerMember.getLoginId());
            products.add(product);
        }

        int userIndex = 0;


        for (Product product : products) {
            int price = product.getAuction().getStartPrice();
            for (int c = 0; c < memberCnt / productCnt; c++) {
                if (c % 3 == 0) {
                    price += priceUnit;
                }
                Member member = members.get(userIndex++);
                Long auctionId = product.getAuction().getId();
                TokenDTO loginToken = memberService.login(member.getLoginId(), "password" + userIndex);

                writer.writeNext(new String[]{member.getLoginId(), String.valueOf(member.getId()), loginToken.getAccessToken(), String.valueOf(price), String.valueOf(product.getId()), String.valueOf(auctionId), String.valueOf(product.getCategory().getId())});
                // db에 저장할 데이터 -> member.id, bidPrice, auction.id
                dummyRepository.save(Dummy.builder()
                        .memberId(member.getId())
                        .auctionId(auctionId)
                        .bidPrice(price)
                        .build()
                );
            }
        }
    }

    private Product insertProduct(String name, String info, Long categoryId, LocalDateTime startDate, LocalDateTime endDate, int startPrice, int priceUnit, String sigFileName, List<String> ordinalFileNames, String loginId) {
        UploadFileDTO sigUpload = uploadFile(FileSubPath.PRODUCT_IMG_PATH, sigFileName);
        List<UploadFileDTO> ordinalUpload = uploadFiles(FileSubPath.PRODUCT_IMG_PATH, ordinalFileNames);

        ProductRegisterDTO productRegister = new ProductRegisterDTO(name, info, categoryId, sigUpload, ordinalUpload, loginId);
        AuctionRegisterDTO auctionRegister = new AuctionRegisterDTO(endDate, startPrice, priceUnit);
        Long savedId = productService.register(productRegister, auctionRegister);
        Product findProduct = productRepository.findById(savedId).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct.getAuction().changeAuctionStartDate(startDate);
        findProduct.changeCreatedDate(startDate);

        return findProduct;
    }

    private UploadFileDTO uploadFile(String subPath, String fileName) {
        return new UploadFileDTO(fileName, fileName, fileName, fileName);
    }

    private List<UploadFileDTO> uploadFiles(String subPath, List<String> fileNames) {
        return fileNames.stream().map(fileName -> uploadFile(subPath, fileName)).collect(Collectors.toList());
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    private Authority createAuthority(String name) {
        Optional<Authority> roleUser = authorityRepository.findById(name);
        if (roleUser.isEmpty()) {
            Authority authority = Authority.builder()
                    .authorityName(name)
                    .build();
            em.persist(authority);
            return authority;
        }
        return roleUser.get();
    }

    private Member createMember(String name, String birth, String email, String loginId, String password, String phoneNumber, Authority authorities) {
        return Member.builder()
                .name(name)
                .birth(birth)
                .email(email)
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .authorities(Collections.singleton(authorities))
                .build();
    }
}
