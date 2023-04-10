package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.domain.file.QuestionStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.QuestionErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.QuestionRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.service.dto.QuestionRegisterDTO;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFileDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuestionServiceTest {

    @Autowired
    private S3FileUploader fileUploader;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionService questionService;

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
    @DisplayName("질문 등록 성공")
    void register() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        // 상품 등록
        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller.getLoginId());
        Long savedProductId = productService.register(registerDTO);
        String parentContent = "질문 있습니다1";
        String childContent = "질문 있습니다2";

        //when
        // 부모 댓글
        Long savedQuestionId1 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId, buyer.getLoginId()));
        //자식 댓글
        Long savedQuestionId2 = questionService.registerQuestion(new QuestionRegisterDTO(savedQuestionId1, childContent, savedProductId, seller.getLoginId()));

        //then
        Question findQuestion1 = questionRepository.findById(savedQuestionId1).orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
        Question findQuestion2 = questionRepository.findById(savedQuestionId2).orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));

        // 부모 댓글
        assertThat(findQuestion1.getParent()).isNull();
        assertThat(findQuestion1.getContent()).isEqualTo(parentContent);
        assertThat(findQuestion1.getLayer()).isEqualTo(0);
        assertThat(findQuestion1.getMember()).isSameAs(buyer);

        //자식 댓글
        assertThat(findQuestion2.getParent().getId()).isEqualTo(savedQuestionId1);
        assertThat(findQuestion2.getContent()).isEqualTo(childContent);
        assertThat(findQuestion2.getLayer()).isEqualTo(1);
        assertThat(findQuestion2.getMember()).isSameAs(seller);
    }

    @Test
    @DisplayName("질문 등록 실패, 작성자 존재 / 상품 존재하지 않는 경우")
    void registerFail1() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        buyer.changeStatus(MemberStatus.DELETED);
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        // 상품 등록
        //작성 상품이 존재하지 않는 경우
        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller.getLoginId());
        Long savedProductId1 = productService.register(registerDTO1);
        Product findProduct = productRepository.findById(savedProductId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct.changeProductStatus(ProductStatus.DELETED);

        //작성자가 존재하지 않는 경우
        ProductRegisterDTO registerDTO2 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller.getLoginId());
        Long savedProductId2 = productService.register(registerDTO2);

        String parentContent = "질문 있습니다1";

        //then
        //작성 상품이 존재하지 않는 경우
        assertThatThrownBy(() -> questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId1, seller.getLoginId())))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());

        //작성자가 존재하지 않는 경우
        assertThatThrownBy(() -> questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId2, buyer.getLoginId())))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("질문 등록 실패, 대댓글 작성자가 판매자가 아닌 경우/ 대댓글까지만 작성이 가능")
    void registerFail2() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller.getLoginId());
        Long savedProductId1 = productService.register(registerDTO1);

        String parentContent = "질문이요";
        String childContent = "질문 답글이요";
        // 부모 댓글 작성
        Long parentId = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId1, buyer.getLoginId()));

        //자식 댓글 작성
        Long childId = questionService.registerQuestion(new QuestionRegisterDTO(parentId, childContent, savedProductId1, seller.getLoginId()));

        //then
        //작성 상품이 존재하지 않는 경우
        assertThatThrownBy(() -> questionService.registerQuestion(new QuestionRegisterDTO(parentId, childContent, savedProductId1, buyer.getLoginId())))
                .isInstanceOf(CustomException.class)
                .hasMessage(QuestionErrorCode.QUESTION_WRITE_SELLER_ONLY.getMessage());

        //대대댓글 작성하는 경우(대댓글까지만 작성 가능)
        assertThatThrownBy(() -> questionService.registerQuestion(new QuestionRegisterDTO(childId, childContent, savedProductId1, buyer.getLoginId())))
                .isInstanceOf(CustomException.class)
                .hasMessage(QuestionErrorCode.INVALID_LAYER_QUESTION.getMessage());
    }

    @Test
    @DisplayName("질문 삭제 성공")
    void delete() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        // 상품 등록
        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller.getLoginId());
        Long savedProductId = productService.register(registerDTO);
        String parentContent = "질문 있습니다1";
        String childContent = "질문 있습니다2";

        // 부모 댓글
        Long savedQuestionId1 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId, buyer.getLoginId()));
        //자식 댓글
        Long savedQuestionId2 = questionService.registerQuestion(new QuestionRegisterDTO(savedQuestionId1, childContent, savedProductId, seller.getLoginId()));

        //when
        Long deletedQuestionId1 = questionService.deleteQuestion(savedQuestionId1, buyer.getLoginId());
        Long deletedQuestionId2 = questionService.deleteQuestion(savedQuestionId2,seller.getLoginId());

        //then
        Question findQuestion1 = questionRepository.findById(deletedQuestionId1).orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
        Question findQuestion2 = questionRepository.findById(deletedQuestionId2).orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
        assertThat(findQuestion1.getStatus()).isEqualTo(QuestionStatus.DELETED);
        assertThat(findQuestion2.getStatus()).isEqualTo(QuestionStatus.DELETED);

    }

    @Test
    @DisplayName("질문 삭제 실패, 질문이 존재하지 않는 경우/ 질문 작성자가 아닌 회원이 제거하는 경우")
    void deleteFail() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member seller = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        // 상품 등록
        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller.getLoginId());
        Long savedProductId1 = productService.register(registerDTO);

        String content = "질문 있습니다1";

        Long savedQuestionId1 = questionService.registerQuestion(new QuestionRegisterDTO(null, content, savedProductId1, buyer.getLoginId()));

        Long savedQuestionId2 = questionService.registerQuestion(new QuestionRegisterDTO(null, content, savedProductId1, buyer.getLoginId()));
        Question findQuestion2 = questionRepository.findById(savedQuestionId2).orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
        findQuestion2.changeStatus(QuestionStatus.DELETED
        );
        //then
        // 질문 작성자가 아닌 회원이 질문 삭제하려는 경우
        assertThatThrownBy(() -> questionService.deleteQuestion(savedQuestionId1, seller.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.INVALID_USER.getMessage());
        // 질문이 존재하지 않는 경우
        assertThatThrownBy(() -> questionService.deleteQuestion(savedQuestionId2, buyer.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(QuestionErrorCode.QUESTION_NOT_FOUND.getMessage());
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