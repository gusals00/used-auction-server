package com.auction.usedauction;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionHistoryErrorCode;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.file.FileRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.AuctionHistoryService;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.QuestionService;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.service.dto.AuctionRegisterDTO;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.service.dto.QuestionRegisterDTO;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFileDTO;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Profile(value = {"local", "production"})
public class InitDBService {

    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3FileUploader fileUploader;
    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final AuctionHistoryService auctionHistoryService;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final EntityManager em;
    private final QuestionService questionService;

    @Value("${INIT_FILE_PATH}")
    private String filePath;

    @Transactional
    public void initDb() {

        //Category 추가
        insertCategory();

        // member + Authority ROLE_USER 추가
        insertMember();

        // 상품 추가
        insertProducts();

        //질문 추가
        insertQuestions();
    }

    private void insertQuestions() {
        Member member1 = memberRepository.findByLoginId("hyeonmin").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member2 = memberRepository.findByLoginId("11").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member3 = memberRepository.findByLoginId("20180004").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Product findProduct1 = productRepository.findByName("갤럭시 북 3 팝니다").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Long parentId1 = questionService.registerQuestion(new QuestionRegisterDTO(null, "구매한지 얼마나 되었나요?", findProduct1.getId(), member3.getLoginId()));
        questionService.registerQuestion(new QuestionRegisterDTO(parentId1, "한 일주일 정도 된거 같아요", findProduct1.getId(), member2.getLoginId()));
        questionService.registerQuestion(new QuestionRegisterDTO(parentId1, "그리고 한번도 사용한 적 없어요", findProduct1.getId(), member2.getLoginId()));

        Long parentId2 = questionService.registerQuestion(new QuestionRegisterDTO(null, "갤럭시 북 2는 없나요?", findProduct1.getId(), member1.getLoginId()));
        questionService.registerQuestion(new QuestionRegisterDTO(parentId2, "중고로 있긴 한데... 좀 오래되서요", findProduct1.getId(), member2.getLoginId()));
        questionService.registerQuestion(new QuestionRegisterDTO(parentId2, "만약 사실 의향 있으시면 채팅으로 연락 주세요", findProduct1.getId(), member2.getLoginId()));



    }

    private void insertMember() {
        Authority authority = createAuthority("ROLE_USER");
        em.persist(authority);

        Member member1 = createMember("현민", "990828", "a111@naver.com", "hyeonmin", "password", "010-1233-1233", authority);
        Member member2 = createMember("병관", "990128", "ab@naver.com", "11", "11", "010-2222-3333", authority);
        Member member3 = createMember("대현", "990428", "addd@naver.com", "20180004", "1128", "010-4444-8888", authority);

        memberRepository.saveAll(Arrays.asList(member1, member2, member3));
    }

    private void insertProducts() {
        // 회원조회
        Member member1 = memberRepository.findByLoginId("hyeonmin").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member2 = memberRepository.findByLoginId("11").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member3 = memberRepository.findByLoginId("20180004").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now().withSecond(0);

        //카테고리 조회
        Category bookCategory = categoryRepository.findCategoryByName("도서").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category ticketCategory = categoryRepository.findCategoryByName("티켓/교환권").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category digitalCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));


        //member1 상품 저장
        Product findProduct1 = insertProduct("이것이 코딩 테스트다", "책 정보입니다", bookCategory.getId(), now.minusDays(4), now.minusDays(2), 10000, 2000,
                "1_1.jpg", Arrays.asList("1_2.jpg", "1_3.jpg", "1_4.jpg"), member1.getLoginId(), 3);
        // member3이 낙찰됨
        AuctionHistory auctionHistory1 = bidAuction(findProduct1.getAuction().getId(), 20000, member3.getLoginId(), LocalDateTime.now().minusDays(3));
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct1.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);


        Product findProduct2 = insertProduct("한화 이글스 티켓", "티켓 정보입니다", ticketCategory.getId(), now.minusDays(7), now.minusDays(4), 100000, 20000,
                "2_1.jpg", Arrays.asList("2_2.jpg"), member1.getLoginId(), 10);
        //member3 입찰
        bidAuction(findProduct2.getAuction().getId(), 120000, member3.getLoginId(), LocalDateTime.now().minusDays(6));
        //member2 낙찰
        AuctionHistory auctionHistory3 = bidAuction(findProduct2.getAuction().getId(), 140000, member2.getLoginId(), LocalDateTime.now().minusDays(5));
        auctionHistory3.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct2.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);


        //member2 상품 저장
        insertProduct("자바만 잡아도 팝니다", "자바만 잡아도 정보입니다", bookCategory.getId(), now.plusDays(7), 20000, 2000,
                "3_1.jpg", Arrays.asList("3_2.jpg"), member2.getLoginId(), 2);

        insertProduct("갤럭시 북 3 팝니다", "갤럭시 북이고 상태 좋습니다", digitalCategory.getId(), now.plusDays(6), 1000000, 100000,
                "4_1.jpg", Arrays.asList("4_2.jpg"), member2.getLoginId(), 15);

        //member3 상품 저장
        // 경매 중
        Product findProduct3 = insertProduct("객체지향의 사실과 오해1", "객체지향의 사실과 오해1 새책입니다.", bookCategory.getId(), now.plusDays(2), 15000, 1000,
                "5_1.jpg", Arrays.asList("5_2.jpg", "5_3.jpg"), member3.getLoginId(), 11);
        //meber2 입찰
        bidAuction(findProduct3.getAuction().getId(), 16000, member2.getLoginId(), LocalDateTime.now().plusDays(2));

        // 낙찰 실패
        Product findProduct4 = insertProduct("객체지향의 사실과 오해2", "객체지향의 사실과 오해2 새책입니다.", bookCategory.getId(), now.minusDays(10), now.minusDays(4), 21000, 1000,
                "5_1.jpg", Arrays.asList("5_2.jpg", "5_3.jpg"), member3.getLoginId(), 7);
        findProduct4.getAuction().changeAuctionStatus(AuctionStatus.FAIL_BID);

        // 낙찰 성공
        Product findProduct5 = insertProduct("로지텍 마우스 팝니다1", "로지텍 마우스1고 상태 좋습니다.", digitalCategory.getId(), now.minusDays(10), now.minusDays(4), 22000, 1000,
                "6_1.jpg", Arrays.asList("6_2.jpg"), member3.getLoginId(), 12);
        //member1 입찰
        bidAuction(findProduct5.getAuction().getId(), 25000, member1.getLoginId(), LocalDateTime.now().minusDays(6));
        //member2 낙찰
        AuctionHistory auctionHistory4 = bidAuction(findProduct5.getAuction().getId(), 140000, member2.getLoginId(), LocalDateTime.now().minusDays(5));
        auctionHistory4.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct5.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        // 거래 실패
        Product findProduct6 = insertProduct("로지텍 마우스 팝니다2", "로지텍 마우스2고 상태 좋습니다.", digitalCategory.getId(), now.minusDays(14), now.minusDays(1), 15000, 1000,
                "6_1.jpg", Arrays.asList("6_2.jpg"), member3.getLoginId(), 15);
        //member2 낙찰/ 구매자가 거래 불발
        AuctionHistory auctionHistory5 = bidAuction(findProduct6.getAuction().getId(), 20000, member2.getLoginId(), LocalDateTime.now().minusDays(5));
        auctionHistory5.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct6.getAuction().changeAuctionStatus(AuctionStatus.TRANSACTION_FAIL);
        findProduct6.getAuction().changeBuyerStatus(TransStatus.TRANS_REJECT);
        findProduct6.getAuction().changeSellerStatus(TransStatus.TRANS_COMPLETE);

        // 거래성공
        Product findProduct7 = insertProduct("로지텍 마우스 팝니다3", "로지텍 마우스3고 상태 좋습니다.", digitalCategory.getId(),  now.minusDays(7), now.minusDays(1), 15000, 1000,
                "6_1.jpg", Arrays.asList("6_2.jpg"), member3.getLoginId(), 14);
        //member2 낙찰/ 거래 완료
        AuctionHistory auctionHistory6 = bidAuction(findProduct7.getAuction().getId(), 20000, member2.getLoginId(), LocalDateTime.now().minusDays(5));
        auctionHistory6.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct7.getAuction().changeAuctionStatus(AuctionStatus.TRANSACTION_OK);
        findProduct7.getAuction().changeBuyerStatus(TransStatus.TRANS_COMPLETE);
        findProduct7.getAuction().changeSellerStatus(TransStatus.TRANS_COMPLETE);
    }

    private AuctionHistory bidAuction(Long auctionId, int bidPrice, String memberLoginId, LocalDateTime bidDate) {
        AuctionBidResultDTO auctionBidResultDTO2 = auctionHistoryService.biddingAuction(auctionId, bidPrice, memberLoginId);
        AuctionHistory auctionHistory = auctionHistoryRepository.findById(auctionBidResultDTO2.getAuctionHistoryId()).orElseThrow(() -> new CustomException(AuctionHistoryErrorCode.AUCTION_HISTORY_NOT_FOUND));
        auctionHistory.changeCreatedDate(bidDate);
        return auctionHistory;
    }

    private AuctionHistory bidAuction(Long auctionId, int bidPrice, String memberLoginId) {
        AuctionBidResultDTO auctionBidResultDTO2 = auctionHistoryService.biddingAuction(auctionId, bidPrice, memberLoginId);
        AuctionHistory auctionHistory = auctionHistoryRepository.findById(auctionBidResultDTO2.getAuctionHistoryId()).orElseThrow(() -> new CustomException(AuctionHistoryErrorCode.AUCTION_HISTORY_NOT_FOUND));
        return auctionHistory;
    }

    private Product insertProduct(String name, String info, Long categoryId, LocalDateTime endDate, int startPrice, int priceUnit, String sigFileName, List<String> ordinalFileNames, String loginId, int viewCount) {
        UploadFileDTO sigUpload = uploadFile(FileSubPath.PRODUCT_IMG_PATH, sigFileName);
        List<UploadFileDTO> ordinalUpload = uploadFiles(FileSubPath.PRODUCT_IMG_PATH, ordinalFileNames);

        ProductRegisterDTO productRegister = new ProductRegisterDTO(name, info, categoryId, sigUpload, ordinalUpload, loginId);
        AuctionRegisterDTO auctionRegister = new AuctionRegisterDTO(endDate, startPrice, priceUnit);
        Long savedId = productService.register(productRegister, auctionRegister);
        Product findProduct = productRepository.findById(savedId).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        for (int i = 0; i < viewCount; i++) { // 조회수 증가
            findProduct.increaseViewCount();
        }
        return findProduct;
    }

    private Product insertProduct(String name, String info, Long categoryId, LocalDateTime startDate, LocalDateTime endDate, int startPrice, int priceUnit, String sigFileName, List<String> ordinalFileNames, String loginId, int viewCount) {
        UploadFileDTO sigUpload = uploadFile(FileSubPath.PRODUCT_IMG_PATH, sigFileName);
        List<UploadFileDTO> ordinalUpload = uploadFiles(FileSubPath.PRODUCT_IMG_PATH, ordinalFileNames);

        ProductRegisterDTO productRegister = new ProductRegisterDTO(name, info, categoryId, sigUpload, ordinalUpload, loginId);
        AuctionRegisterDTO auctionRegister = new AuctionRegisterDTO(endDate, startPrice, priceUnit);
        Long savedId = productService.register(productRegister, auctionRegister);
        Product findProduct = productRepository.findById(savedId).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct.changeCreatedDate(startDate);
        for (int i = 0; i < viewCount; i++) { // 조회수 증가
            findProduct.increaseViewCount();
        }
        return findProduct;
    }

    private UploadFileDTO uploadFile(String subPath, String fileName) {
        return fileUploader.uploadFile(new File(filePath + fileName), subPath);
    }

    private List<UploadFileDTO> uploadFiles(String subPath, List<String> fileNames) {
        return fileNames.stream().map(fileName -> uploadFile(subPath, fileName)).collect(Collectors.toList());
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

    private Authority createAuthority(String name) {
        return Authority.builder()
                .authorityName(name)
                .build();
    }

    @Transactional
    public void insertCategory() {
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

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    @PreDestroy
    public void deleteS3File() {
        // S3에 저장된 파일 삭제
        fileRepository.findAll()
                .forEach(file -> fileUploader.deleteFile(file.getPath()));
    }

}
