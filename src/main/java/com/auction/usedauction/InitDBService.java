package com.auction.usedauction;

import com.auction.usedauction.aop.S3Rollback;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.*;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.NotificationRepository;
import com.auction.usedauction.repository.auction_end.AuctionEndRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.chat.ChatMessageRepository;
import com.auction.usedauction.repository.chat.ChatRoomRepository;
import com.auction.usedauction.repository.dto.ProductIdAndLoginIds;
import com.auction.usedauction.repository.file.FileRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import com.auction.usedauction.repository.query.AuctionQueryRepository;
import com.auction.usedauction.scheduler.EndOfAuctionBidScheduler;
import com.auction.usedauction.scheduler.TransCompleteScheduler;
import com.auction.usedauction.service.*;
import com.auction.usedauction.service.dto.AuctionBidResultDTO;
import com.auction.usedauction.service.dto.AuctionRegisterDTO;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.service.dto.QuestionRegisterDTO;
import com.auction.usedauction.util.s3.FileSubPath;
import com.auction.usedauction.util.s3.S3FileUploader;
import com.auction.usedauction.util.s3.UploadFileDTO;
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

import static com.auction.usedauction.domain.NotificationType.BUYER_TRANS_CONFIRM;
import static com.auction.usedauction.domain.NotificationType.SELLER_TRANS_CONFIRM;
import static com.auction.usedauction.exception.error_code.UserErrorCode.USER_NOT_FOUND;

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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuctionEndRepository auctionEndRepository;
    private final AuctionQueryRepository auctionQueryRepository;
    private final AuctionService auctionService;
    private final EndOfAuctionBidScheduler endOfAuctionBidScheduler;
    private final TransCompleteScheduler transCompleteScheduler;
    private final FileService fileService;
    private final AuctionHistoryQueryRepository auctionHistoryQueryRepository;
    private final NotificationRepository notificationRepository;

    @Value("${INIT_FILE_PATH}")
    private String filePath;

    @Transactional
    @S3Rollback
    public void init() {
        //Category 추가
        insertCategory();

        // member + Authority ROLE_USER 추가
        insertMember();

        // 상품 추가
        insertProducts();

        //질문 추가
        insertQuestions();

        //채팅방, 메세지 추가
        insertChatRoomsAndMessages();

        //알림 추가
        insertNotifications();
    }

    public void initScheduler() {
        //로딩 시점에 내일 경매 종료되는 경매 저장
        initTodayAuctionEnd();
        //현재 시간 기준으로 경매 종료시 경매 상태를 변경
        endOfAuctionBidScheduler.changeAuctionStatusToAuctionEndStatuses();
        transCompleteScheduler.checkTransCompleteAndBan();
    }

    private void initTodayAuctionEnd() {
        log.info("로딩 시점에 내일 경매 종료되는 경매 저장");
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(1).withMinute(59).withSecond(59);
        auctionEndRepository.add(auctionQueryRepository.findIdAndEndDateByDate(startDate, endDate));
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

        Member member1 = createMember("김현민", "990828", "hyeonmin@kumoh.ac.kr", "hyeonmin", "password", "010-1233-1233", authority);
        Member member2 = createMember("강병관", "990128", "bkKang@kumoh.ac.kr", "11", "11", "010-2222-3333", authority);
        Member member3 = createMember("강대현", "961128", "daehyeon1128@kumoh.ac.kr", "20180004", "1128", "010-4444-8888", authority);
        Member member4 = createMember("성호창", "990622", "hoChang@kumoh.ac.kr", "20180584", "tjdghckd11", "010-1444-8848", authority);
        Member member5 = createMember("여시철", "990428", "sisisi12@kumoh.ac.kr", "20185444", "tlcjf11", "010-4443-4888", authority);
        Member member6 = createMember("권성수", "990128", "sungsu@kumoh.ac.kr", "20180022", "tjdtn11", "010-4243-4838", authority);
        Member member7 = createMember("김광민", "990124", "kwnagmin@kumoh.ac.kr", "kwangmin", "rhkdals11", "010-1243-1838", authority);

        memberRepository.saveAll(Arrays.asList(member1, member2, member3, member4, member5, member6, member7));
    }

    private void insertChatRoomsAndMessages() {
        Member member1 = memberRepository.findByLoginId("hyeonmin").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member2 = memberRepository.findByLoginId("11").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member3 = memberRepository.findByLoginId("20180004").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Product findProduct1 = productRepository.findByName("로지텍 마우스 팝니다1").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Product findProduct2 = productRepository.findByName("한화 이글스 티켓").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Product findProduct3 = productRepository.findByName("이것이 코딩 테스트다").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        ChatRoom chatRoom1 = createChatRoom(member1, findProduct1);
        ChatRoom chatRoom2 = createChatRoom(member2, findProduct2);
        ChatRoom chatRoom3 = createChatRoom(member3, findProduct3);

        chatRoomRepository.saveAll(Arrays.asList(chatRoom1, chatRoom2, chatRoom3));

        ChatMessage chatMessage1 = createChatMessage("안녕하세요", chatRoom1, member1, true);
        ChatMessage chatMessage2 = createChatMessage("네 안녕하세요", chatRoom1, findProduct1.getMember(), false);
        ChatMessage chatMessage3 = createChatMessage("안녕하세요", chatRoom2, member2, true);
        ChatMessage chatMessage4 = createChatMessage("하이", chatRoom2, findProduct2.getMember(), false);
        ChatMessage chatMessage5 = createChatMessage("안녕하세요", chatRoom3, member3, true);
        ChatMessage chatMessage6 = createChatMessage("그래", chatRoom3, findProduct3.getMember(), false);

        chatMessageRepository.saveAll(Arrays.asList(chatMessage1, chatMessage2, chatMessage3, chatMessage4, chatMessage5, chatMessage6));
    }

    private void insertProducts() {
        // 회원조회
        Member member1 = memberRepository.findByLoginId("hyeonmin").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member2 = memberRepository.findByLoginId("11").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member3 = memberRepository.findByLoginId("20180004").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member4 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member5 = memberRepository.findByLoginId("20185444").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member6 = memberRepository.findByLoginId("20180022").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member7 = memberRepository.findByLoginId("kwangmin").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now().withSecond(0);
        log.info("회원 저장 완료");
        //카테고리 조회
        Category bookCategory = categoryRepository.findCategoryByName("도서").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category ticketCategory = categoryRepository.findCategoryByName("티켓/교환권").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category digitalCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category appliancesCategory = categoryRepository.findCategoryByName("생활가전").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category furnitureCategory = categoryRepository.findCategoryByName("가구/인테리어").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category gameCategory = categoryRepository.findCategoryByName("취미/게임/음반").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        log.info("카테고리 저장 완료");


        //member1 상품 저장
        Product findProduct1 = insertProduct("이것이 코딩 테스트다", "책 정보입니다", bookCategory.getId(), now.minusDays(14), now.minusDays(10), 10000, 2000,
                "1_1.jpg", Arrays.asList("1_2.jpg", "1_3.jpg", "1_4.jpg"), member1.getLoginId(), 3);
        log.info("이것이 코딩 테스트다 저장 완료");
        // member3이 낙찰됨
        AuctionHistory auctionHistory1 = bidAuction(findProduct1.getAuction().getId(), 20000, member3.getLoginId(), LocalDateTime.now().minusDays(12));
        log.info("member3이 입찰");
        auctionHistory1.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct1.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        log.info("member3이 낙찰");


        Product findProduct2 = insertProduct("한화 이글스 티켓", "티켓 정보입니다", ticketCategory.getId(), now.minusDays(15), now.minusDays(9), 100000, 20000,
                "2_1.jpg", Arrays.asList("2_2.jpg"), member1.getLoginId(), 10);
        //member3 입찰
        bidAuction(findProduct2.getAuction().getId(), 120000, member3.getLoginId(), LocalDateTime.now().minusDays(12));
        //member2 낙찰
        AuctionHistory auctionHistory3 = bidAuction(findProduct2.getAuction().getId(), 140000, member2.getLoginId(), LocalDateTime.now().minusDays(10));
        auctionHistory3.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct2.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        Product product_member1_2 = insertProduct("레트로 옛날 게임기 팔아요", "사진보시면 600여가지 게임 들어있어요 버튼 여유분 들어있고요 전원선.HDMI케이블 같이 드려요\n전 티비에 연결해서 사용했어요", digitalCategory.getId(), now.plusDays(10), 80000, 1000,
                "21_1.jpg", Arrays.asList("21_2.jpg","21_3.jpg"), member1.getLoginId(), 1);
        Product product_member1_3 = insertProduct("Lightning Digital AV 어댑터 판매합니다", "Lightning Digital AV 어댑터 판매합니다\n아이패드프로와 연결해서 화의, 유튜브감상용으로 사용가능합니가.\n기변으로 인한 판매입니다~!", digitalCategory.getId(), now.plusDays(14), 40000, 3000,
                "22_1.jpg", Arrays.asList("22_2.jpg","22_3.jpg"), member1.getLoginId(), 7);
        Product product_member1_4 = insertProduct("네트워크 튜너 (스마트폰으로 TV보기)", "스마트폰으로 유선 TV볼 수 있게 해주는 기계입니다.\n구매는 한달 전에 했고 실 사용시간은 2시간도 안됩니다.집에서 태블릿으로 TV보는 용도로 구매했는데 TV를 사게 되서 판매합니다", digitalCategory.getId(), now.plusDays(9), 32000, 4000,
                "23_1.jpg", Arrays.asList("23_2.jpg","23_3.jpg"), member1.getLoginId(), 5);
        Product product_member1_5 = insertProduct("sk 스마트빔 (2-3번 사용)", "인터넷 최저가 검색 후 확인해주세요:)\n\n구입한지는 1년정도 됐지만 2-3번? 사용했을 정도로\n새제품과 같습니다 ㅠ 박스에 넣어 보관해서 생활기스도 거의 없습니다 !", digitalCategory.getId(), now.plusDays(10), 160000, 10000,
                "24_1.jpg", Arrays.asList("24_2.jpg"), member1.getLoginId(), 2);
        Product product_member1_6 = insertProduct("공기청정기 새제품", "박스만 없는 새 제품 공기청정기입니다.\n컬러도 아주 예뻐요.", appliancesCategory.getId(), now.minusDays(7), now.plusDays(12), 70000, 5000,
                "25_1.jpg", Arrays.asList("25_1.jpg"), member1.getLoginId(), 2);
        Product product_member1_7 = insertProduct("샤오미 플러그 멀티탭 4구 개별스위치+ USB3구 1.6M 접지코드 해외 가전제품 호환", "단종 제품 국내 전자제품 및 해외직구 전자제품 돼지코 변환 어댑터 구입 없이 사용 외관상 튀어 나올일 없고 변환어댑터는 화제 문제로 변환어댑터 없이 이용 추천 드립니다.", appliancesCategory.getId(), now.minusDays(7), now.plusDays(5), 20000, 5000,
                "26_1.jpg", Arrays.asList("26_1.jpg"), member1.getLoginId(), 8);
        log.info("member1 상품 저장 완료");

        //member2 상품 저장
        Product product_member2_1 = insertProduct("자바만 잡아도 팝니다", "자바만 잡아도 정보입니다", bookCategory.getId(), now.plusDays(7), 20000, 2000,
                "3_1.jpg", Arrays.asList("3_2.jpg"), member2.getLoginId(), 2);
        Product product_member2_2 = insertProduct("갤럭시 북 3 팝니다", "갤럭시 북이고 상태 좋습니다", digitalCategory.getId(), now.plusDays(6), 1000000, 100000,
                "4_1.jpg", Arrays.asList("4_2.jpg"), member2.getLoginId(), 15);

        log.info("member2 상품 저장 완료");

        //member3 상품 저장
        // 경매 중
        Product findProduct3 = insertProduct("객체지향의 사실과 오해1", "객체지향의 사실과 오해1 새책입니다.", bookCategory.getId(), now.plusDays(2), 15000, 1000,
                "5_1.jpg", Arrays.asList("5_2.jpg", "5_3.jpg"), member3.getLoginId(), 11);

        //meber2 입찰
        bidAuction(findProduct3.getAuction().getId(), 16000, member2.getLoginId(), LocalDateTime.now().plusDays(2));

        // 낙찰 실패
        Product findProduct4 = insertProduct("객체지향의 사실과 오해2", "객체지향의 사실과 오해2 새책입니다.", bookCategory.getId(), now.minusDays(18), now.minusDays(16), 21000, 1000,
                "5_1.jpg", Arrays.asList("5_2.jpg", "5_3.jpg"), member3.getLoginId(), 7);
        findProduct4.getAuction().changeAuctionStatus(AuctionStatus.FAIL_BID);

        // 낙찰 성공
        Product findProduct5 = insertProduct("로지텍 마우스 팝니다1", "로지텍 마우스1고 상태 좋습니다.", digitalCategory.getId(), now.minusDays(10), now.minusDays(5), 22000, 1000,
                "6_1.jpg", Arrays.asList("6_2.jpg"), member3.getLoginId(), 12);
        //member1 입찰
        bidAuction(findProduct5.getAuction().getId(), 25000, member1.getLoginId(), LocalDateTime.now().minusDays(7));
        //member2 낙찰
        AuctionHistory auctionHistory4 = bidAuction(findProduct5.getAuction().getId(), 40000, member2.getLoginId(), LocalDateTime.now().minusDays(6));
        auctionHistory4.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct5.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        // 거래 실패
        Product findProduct6 = insertProduct("로지텍 마우스 팝니다2", "로지텍 마우스2고 상태 좋습니다.", digitalCategory.getId(), now.minusDays(14), now.minusDays(1), 15000, 1000,
                "6_1.jpg", Arrays.asList("6_2.jpg"), member3.getLoginId(), 15);
        //member2 낙찰/ 구매자가 거래 불발
        AuctionHistory auctionHistory5 = bidAuction(findProduct6.getAuction().getId(), 20000, member2.getLoginId(), LocalDateTime.now().minusDays(5));
        auctionHistory5.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct6.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auctionService.memberTransConfirm(findProduct6.getAuction().getId(), findProduct6.getMember().getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(findProduct6.getAuction().getId(), member2.getLoginId(), TransStatus.TRANS_REJECT);

        // 거래성공
        Product findProduct7 = insertProduct("로지텍 마우스 팝니다3", "로지텍 마우스3고 상태 좋습니다.", digitalCategory.getId(), now.minusDays(7), now.minusDays(1), 15000, 1000,
                "6_1.jpg", Arrays.asList("6_2.jpg"), member3.getLoginId(), 14);
        //member2 낙찰/ 거래 완료
        AuctionHistory auctionHistory6 = bidAuction(findProduct7.getAuction().getId(), 20000, member2.getLoginId(), LocalDateTime.now().minusDays(5));
        auctionHistory6.changeStatus(AuctionHistoryStatus.SUCCESSFUL_BID);
        findProduct7.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        auctionService.memberTransConfirm(findProduct7.getAuction().getId(), findProduct7.getMember().getLoginId(), TransStatus.TRANS_COMPLETE);
        auctionService.memberTransConfirm(findProduct7.getAuction().getId(), member2.getLoginId(), TransStatus.TRANS_COMPLETE);

        Product findProduct8 = insertProduct("기초를 탄탄히 세워주는 컴퓨터 사이언스 중고 팝니다", "밑줄 약간 있습니다.", bookCategory.getId(), now.minusDays(7), now.plusDays(10), 20000, 1000,
                "8_1.jpg", Arrays.asList("8_2.jpg"), member3.getLoginId(), 100);

        log.info("member3 상품 저장 완료");

        //member4 상품 저장
        //경매중
        Product findProduct9 = insertProduct("다이슨 드라이기 팝니다", "한달 정도 사용했습니다.", appliancesCategory.getId(), now.minusDays(7), now.plusDays(10), 300000, 10000,
                "9_2.jpg", Arrays.asList("9_1.jpg"), member4.getLoginId(), 50);
        Product findProduct10 = insertProduct("보다나 물결고데기 32mm", "2번밖에 사용 안했습니다.", appliancesCategory.getId(), now.minusDays(7), now.plusDays(10), 50000, 1000,
                "10_1.png", Arrays.asList("10_2.png", "10_3.png", "10_4.png"), member4.getLoginId(), 7);

        //member5 상품 저장
        //경매중
        Product findProduct11 = insertProduct("데스커 데스크탑 책상 팝니다.", "이사가게 되서 팝니다. 2년 정도 된 상품입니다.", furnitureCategory.getId(), now.minusDays(7), now.plusDays(10), 200000, 10000,
                "11_1.jpg", Arrays.asList("11_2.jpg"), member5.getLoginId(), 2);
        Product findProduct12 = insertProduct("미개봉) 허먼밀러 뉴에어론 라이트플러스, 그라파이트 (+헤드레스트)", "이사가게 되서 팝니다. 미개봉 상품입니다.", furnitureCategory.getId(), now.minusDays(7), now.plusDays(12), 700000, 10000,
                "12_1.jpg", Arrays.asList("12_1.jpg"), member5.getLoginId(), 7);

        //member6 상품 저장
        //경매중
        Product findProduct13 = insertProduct("쿠첸 6인 밥솥 이유식밥솥 미니밥솥", "새상품 구매해서 아이 후기 이유식때 잠깐썼어요 ^^", furnitureCategory.getId(), now.minusDays(2).withHour(13), now.plusDays(12), 35000, 3000,
                "13_1.jpg", Arrays.asList("13_2.jpg", "13_3.jpg"), member6.getLoginId(), 11);
        Product findProduct14 = insertProduct("커피포트", "사용안하고 방치해서 사용감+물때많아요\n 세척해서 가지고사용하실분 가지고가세요", furnitureCategory.getId(), now.minusDays(7).withMinute(20), now.plusDays(7), 35000, 3000,
                "14_1.jpg", Arrays.asList("14_2.jpg", "14_3.jpg"), member6.getLoginId(), 1);

        //member7 상품 저장
        //경매중
        Product findProduct15 = insertProduct("카멜업 보드게임", "7월에 구입해서 3번 놀았습니다\n 그래서 손때 없고 비닐에 보관해서 깨끗합니다", gameCategory.getId(), now.minusDays(1), now.plusDays(5), 10000, 2000,
                "15_1.jpg", Arrays.asList("15_2.jpg", "15_3.jpg"), member7.getLoginId(), 22);
        Product findProduct16 = insertProduct("보드게임 시타델 판매합니다.", "시타델 샀는데 집에 보드게임이 너무 많아서 잘 안 해서 네놓아요. 한 두 번정도 했어요. 거의 새것입니다.", gameCategory.getId(), now.minusDays(1), now.plusDays(5), 12000, 1000,
                "16_1.jpg", Arrays.asList("16_2.jpg", "16_3.jpg"), member7.getLoginId(), 3);
        Product findProduct17 = insertProduct("(미개봉 새상품)Java의 정석 2nd Edition", "Java의 정석 2nd Edition\n 자바의 기초를 다지기 좋은 책으로,\n 제가 신입사원일 때, 보려고 샀었으나 교육을 받게되어 불용되어 내놓습니다.\n 자바의 기초를 다지실 분께서 가져가서 잘 쓰셨으면 좋겠습니다.\n미개봉품으로 새상품입니다.", bookCategory.getId(), now.minusDays(1).withHour(2).withMinute(19), now.plusDays(3).withHour(11), 12000, 1000,
                "17_1.jpg", Arrays.asList("17_2.jpg"), member7.getLoginId(), 17);
        Product findProduct18 = insertProduct("코드로 배우는 스프링 웹 프로젝트", "코드로 배우는 스프링 웹 프로젝트 책 입니당\n사놓고 안열었어요.....^^;;;;;;", bookCategory.getId(), now.minusDays(2).withHour(1).withMinute(50), now.plusDays(5).withHour(13), 18000, 2000,
                "18_1.jpg", Arrays.asList("18_2.jpg", "18_3.jpg", "18_4.jpg"), member7.getLoginId(), 21);
        Product findProduct19 = insertProduct("머신 러닝 교과서 with 파이썬, 사이킷런, 텐서플로", "볼려고 구했는데 해 볼시간이 없을 것 같아 판매합니다.\n새책입니다.", bookCategory.getId(), now.minusDays(4).withHour(10).withMinute(50), now.plusDays(7).withHour(18).withMinute(30), 25000, 4000,
                "19_1.jpg", Arrays.asList("19_2.jpg", "19_3.jpg", "19_4.jpg", "19_5.jpg"), member7.getLoginId(), 16);
        Product findProduct20 = insertProduct("리눅스의 바이블. 리눅스 실전가이드 개정3판 팔아요~", "리눅스책의 바이블 리눅스 실전가이드 판매합니다\n공부하려고 샀는데 프론트엔드 일을 하다보니 볼시간이 없네요 ㅎㅎ\n보시다시피 딱 한번 펼쳐본 새책입니다.", bookCategory.getId(), now.minusDays(4).withHour(17).withMinute(40), now.plusDays(6).withHour(15).withMinute(23), 29000, 1000,
                "20_1.jpg", Arrays.asList("20_2.jpg", "20_3.jpg", "20_4.jpg"), member7.getLoginId(), 16);
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
        findProduct.getAuction().changeAuctionStartDate(startDate);
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

    private void insertCategory() {
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

    private ChatRoom createChatRoom(Member member, Product product) {
        return ChatRoom.builder()
                .member(member)
                .product(product)
                .build();
    }

    private ChatMessage createChatMessage(String message, ChatRoom chatRoom, Member member, boolean read) {
        return ChatMessage.builder()
                .message(message)
                .chatRoom(chatRoom)
                .member(member)
                .readOrNot(read)
                .build();
    }

    private void insertNotifications() {
        Product findProduct1 = productRepository.findByName("한화 이글스 티켓").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Product findProduct2 = productRepository.findByName("이것이 코딩 테스트다").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<ProductIdAndLoginIds> successIds = auctionHistoryQueryRepository.findSellerAndBuyerLoginIdAndAuctionId(List.of(findProduct1.getAuction().getId(), findProduct2.getAuction().getId()));

        successIds.forEach(ids -> {
            String productName = productRepository.findById(ids.getProductId()).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND)).getName();
            String sellerAndBuyer = " 판매자 : " + ids.getSellerName() + ", 구매자 : " + ids.getBuyerName();
            Notification buyTransConfirm = createNotification(BUYER_TRANS_CONFIRM, ids.getProductId(), ids.getBuyerLoginId(), productName, sellerAndBuyer);
            Notification sellTransConfirm = createNotification(SELLER_TRANS_CONFIRM, ids.getProductId(), ids.getSellerLoginId(), productName, sellerAndBuyer);
            notificationRepository.saveAll(List.of(buyTransConfirm, sellTransConfirm));
        });
    }

    private Notification createNotification(NotificationType type, Long productId, String loginId, String title, String content) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return Notification.builder()
                .checked(false)
                .title(title)
                .content(content)
                .member(member)
                .notificationType(type)
                .relatedUrl("productList/productDetail/" + productId)
                .build();
    }

    @PreDestroy
    public void deleteS3File() {
        // S3에 저장된 파일 삭제
        fileRepository.findAll()
                .forEach(file -> fileUploader.deleteFile(file.getPath()));
    }
}
