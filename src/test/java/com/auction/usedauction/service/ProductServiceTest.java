package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.CategoryErrorCode;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.AuctionHistoryRepository;
import com.auction.usedauction.repository.AuthorityRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFileDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private S3FileUploader fileUploader;
    @Autowired
    private AuctionHistoryRepository auctionHistoryRepository;

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
    @DisplayName("상품 등록 성공")
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
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());

        //when
        Long savedId = productService.register(registerDTO);
        Product findProduct = productRepository.findById(savedId)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        //then
        //기본 정보
        assertThat(findProduct.getName()).isEqualTo(registerDTO.getName());
        assertThat(findProduct.getInfo()).isEqualTo(registerDTO.getInfo());
        assertThat(findProduct.getStartPrice()).isEqualTo(registerDTO.getStartPrice());
        assertThat(findProduct.getNowPrice()).isEqualTo(registerDTO.getStartPrice());

        // 판매자,카테고리 비교
        assertThat(findProduct.getMember()).isSameAs(findMember);
        assertThat(findProduct.getCategory()).isSameAs(findCategory);

        //사진 비교
        assertThat(findProduct.getSigImage().getPath()).isEqualTo(sigFileDTO.getStoreUrl());

        String[] ordinalPathList = ordinalFileDTOList.stream().map(UploadFileDTO::getStoreUrl).toArray(String[]::new);
        assertThat(findProduct.getOrdinalImageList().size()).isEqualTo(ordinalFileDTOList.size());
        assertThat(findProduct.getOrdinalImageList()).extracting("path").contains(ordinalPathList);

    }

    @Test
    @DisplayName("상품 등록 실패, 존재하지 않는 판매자 또는 카테고리인 경우")
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

        // 판매자가 존재하지 않는 경우
        Member findMember1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        findMember1.changeStatus(MemberStatus.DELETED);
        Category findCategory1 = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq1 = new ProductRegisterReq("상품이름", "정보", findCategory1.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);
        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq1, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());

        // 카테고리가 존재하지 않는 경우
        Member findMember2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        ProductRegisterReq registerReq2 = new ProductRegisterReq("상품이름", "정보", -1L, LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);
        ProductRegisterDTO registerDTO2 = new ProductRegisterDTO(registerReq2, sigFileDTO, ordinalFileDTOList, findMember2.getLoginId());


        //then
        // 판매자가 존재하지 않는 경우
        assertThatThrownBy(() -> productService.register(registerDTO1))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());

        // 카테고리가 존재하지 않는 경우
        assertThatThrownBy(() -> productService.register(registerDTO2))
                .isInstanceOf(CustomException.class)
                .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void delete() throws Exception{
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
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());

        Long savedId = productService.register(registerDTO);

        //when
        Long deletedId = productService.deleteProduct(savedId, findMember.getLoginId());

        //then
        Product findProduct1 = productRepository.findByIdAndProductStatusNot(deletedId, ProductStatus.DELETED).orElse(null);
        assertThat(findProduct1).isNull();

        Product findProduct2 = productRepository.findById(deletedId).orElseThrow(()->new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        assertThat(findProduct2.getProductStatus()).isEqualTo(ProductStatus.DELETED);

    }

    @Test
    @DisplayName("상품 삭제 실패, 상품 상태가 삭제(DELETED)이거나 낙찰 성공(SUCCESS_BID)인 경우")
    void deleteFail() throws Exception{
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());

        // 상품 저장
        Long savedId1 = productService.register(registerDTO);
        Long savedId2 = productService.register(registerDTO);

        // 상품 상태 변경
        Product savedProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        savedProduct1.changeProductStatus(ProductStatus.DELETED);
        Product savedProduct2 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        savedProduct2.changeProductStatus(ProductStatus.SUCCESS_BID);

        //then

        // 상품 상태가 DELETED인 경우
        assertThatThrownBy(() -> productService.deleteProduct(savedId1, findMember.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.INVALID_DELETE_PRODUCT_STATUS.getMessage());
    }

    @Test
    @DisplayName("상품 삭제 실패, 경매중일 때 입찰 기록이 있는 경우")
    void deleteFail2() throws Exception{
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String contentType = "image/png";

        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());

        // 상품 저장
        Long savedId1 = productService.register(registerDTO);
        Product savedProduct = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 상품 입찰
        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        AuctionHistory auctionHistory = createAuctionHistory(savedProduct, 11000, buyer);
        auctionHistoryRepository.save(auctionHistory);

        //then
        assertThatThrownBy(() -> productService.deleteProduct(savedId1, findMember.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.INVALID_DELETE_PRODUCT_HISTORY.getMessage());

    }

    private AuctionHistory createAuctionHistory(Product product, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .product(product)
                .bidPrice(bidPrice)
                .member(member)
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