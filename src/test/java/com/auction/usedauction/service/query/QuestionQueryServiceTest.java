package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.QuestionErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.QuestionRepository;
import com.auction.usedauction.service.ProductService;
import com.auction.usedauction.service.QuestionService;
import com.auction.usedauction.service.dto.*;
import com.auction.usedauction.util.s3.FileSubPath;
import com.auction.usedauction.util.s3.S3FileUploader;
import com.auction.usedauction.util.s3.UploadFileDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class QuestionQueryServiceTest {

    @Autowired
    private QuestionQueryService questionQueryService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private S3FileUploader fileUploader;
    @Autowired
    private ProductService productService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("질문 조회 성공, 페이징 동작 / 자식 질문이 잘 정렬 되었는지")
    void getQuestions() throws Exception {
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
        Member member1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member3 = memberRepository.findByLoginId("20180004").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, member1.getLoginId());

        ProductRegisterDTO registerDTO2 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, member2.getLoginId());

        Long savedProductId1 = productService.register(registerDTO1,auctionRegisterDTO);
        Long savedProductId2 = productService.register(registerDTO2,auctionRegisterDTO);

        String parentContent = "PARENT";
        String childContent = "CHILD";

        // 부모 댓글
        // savedProductId1 의 부모 댓글
        Long parentId1 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId1, member3.getLoginId()));
        Long parentId2 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId1, member3.getLoginId()));
        Long parentId3 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId1, member3.getLoginId()));
        Long parentId4 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId1, member3.getLoginId()));

        // savedProductId2 의 부모 댓글
        Long parentId6 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId2, member3.getLoginId()));
        Long parentId7 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId2, member3.getLoginId()));
        Long parentId8 = questionService.registerQuestion(new QuestionRegisterDTO(null, parentContent, savedProductId2, member3.getLoginId()));

        //자식 댓글
        //parentId1의 자식댓글
        Long childId1 = questionService.registerQuestion(new QuestionRegisterDTO(parentId1, childContent, savedProductId1, member1.getLoginId()));
        Long childId2 = questionService.registerQuestion(new QuestionRegisterDTO(parentId1, childContent, savedProductId1, member1.getLoginId()));
        Long childId3 = questionService.registerQuestion(new QuestionRegisterDTO(parentId1, childContent, savedProductId1, member1.getLoginId()));
        Question childId3Question = questionRepository.findById(childId3).orElseThrow(() -> new CustomException(QuestionErrorCode.QUESTION_NOT_FOUND));
        childId3Question.changeCreatedDate(LocalDateTime.now().minusDays(1));

        //parentId7의 자식댓글
        Long childId4 = questionService.registerQuestion(new QuestionRegisterDTO(parentId7, childContent, savedProductId2, member2.getLoginId()));
        Long childId5 = questionService.registerQuestion(new QuestionRegisterDTO(parentId7, childContent, savedProductId2, member2.getLoginId()));


        //when

        // 페이징 동작
        // savedProductId1
        PageListRes<QuestionPageContentRes> questionPage1 = questionQueryService.getQuestionPage(PageRequest.of(0, 3), savedProductId1);
        PageListRes<QuestionPageContentRes> questionPage2 = questionQueryService.getQuestionPage(PageRequest.of(1, 3), savedProductId1);
        // savedProductId1
        PageListRes<QuestionPageContentRes> questionPage3 = questionQueryService.getQuestionPage(PageRequest.of(0, 7), savedProductId2);


        //then
        //페이징 동작
        // savedProductId1
        assertThat(questionPage1.getContent()).extracting("questionId").containsExactly(parentId1, parentId2, parentId3);
        assertThat(questionPage1.getNumberOfElements()).isEqualTo(3);
        assertThat(questionPage2.getContent()).extracting("questionId").containsExactly(parentId4);
        assertThat(questionPage2.getNumberOfElements()).isEqualTo(1);
        // savedProductId1
        assertThat(questionPage3.getContent()).extracting("questionId").containsExactly(parentId6, parentId7, parentId8);
        assertThat(questionPage3.getNumberOfElements()).isEqualTo(3);

        //자식 댓글 정렬 확인
        //parentId1의 자식댓글
        assertThat(questionPage1.getContent().get(0).getChildren()).extracting("questionId").containsExactly(childId3,childId1,childId2);
        assertThat(questionPage1.getContent().get(1).getChildren()).isEmpty();
        //parentId7의 자식댓글
        assertThat(questionPage3.getContent().get(0).getChildren()).isEmpty();
        assertThat(questionPage3.getContent().get(1).getChildren()).extracting("questionId").containsExactly(childId4,childId5);


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