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
import com.auction.usedauction.service.dto.ProductUpdateReq;
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

        Product findProduct2 = productRepository.findById(deletedId).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        assertThat(findProduct2.getProductStatus()).isEqualTo(ProductStatus.DELETED);

    }

    @Test
    @DisplayName("상품 삭제 실패, 상품 상태가 삭제(DELETED)이거나 낙찰 성공(SUCCESS_BID)인 경우")
    void deleteFail() throws Exception {
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
    void deleteFail2() throws Exception {
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

    @Test
    @DisplayName("상품 삭제 실패, 삭제하려는 member 가 올바르지 않은 경우")
    void deleteFail3() throws Exception {
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
        Member seller1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member seller2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Category findCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000L, 1000L, ordinalFileList, sigFile);

        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller1.getLoginId());

        // 상품 저장
        Long savedId1 = productService.register(registerDTO);
        Product savedProduct = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));


        //then
        // 판매자가 아닌 회원이 상품 삭제하려는 경우
        assertThatThrownBy(() -> productService.deleteProduct(savedId1, seller2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.INVALID_USER.getMessage());

    }

    @Test
    @DisplayName("상품 수정 성공")
    void update() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String fileName4 = "test4.png";

        String contentType = "image/png";
        String productName = "상품이름";
        String info = "정보";
        LocalDateTime now = LocalDateTime.now();
        Long startPrice = 10000L;
        Long priceUnit = 1000L;
        LocalDateTime savedTime = now.plusDays(2);
        LocalDateTime updatedTime = now.plusMonths(1);
        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        MultipartFile file3 = new MockMultipartFile("testFile3", fileName4, contentType, "test4".getBytes());

        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));
        List<MultipartFile> updatedOrdinalFileList = new ArrayList<>(Arrays.asList(file2, file3));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member findMember = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category updateCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        ProductRegisterReq registerReq = new ProductRegisterReq(productName, info, findCategory.getId(), savedTime, startPrice, priceUnit, ordinalFileList, sigFile);

        // 상품 등록
        ProductRegisterDTO registerDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());
        Long savedId = productService.register(registerDTO);


        //when
        ProductUpdateReq productUpdateReq = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice.intValue(), priceUnit.intValue(), updatedOrdinalFileList, sigFile);
        Long updatedId = productService.updateProduct(savedId, productUpdateReq, findMember.getLoginId());

        //then
        Product findProduct = productRepository.findById(updatedId).
                orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        assertThat(findProduct.getName()).isEqualTo(productName);
        assertThat(findProduct.getInfo()).isEqualTo(info);
        assertThat(findProduct.getCategory()).isSameAs(updateCategory);
        assertThat(findProduct.getAuctionEndDate()).isEqualTo(updatedTime);
        assertThat(findProduct.getStartPrice()).isEqualTo(startPrice.intValue());
        assertThat(findProduct.getPriceUnit()).isEqualTo(priceUnit.intValue());

        // 사진 검증
        // 대표 사진
        assertThat(findProduct.getSigImage().getOriginalName()).isEqualTo(sigFile.getOriginalFilename());
        // 일반 사진
        assertThat(findProduct.getOrdinalImageList().size()).isEqualTo(2);
        assertThat(findProduct.getOrdinalImageList()).extracting("originalName")
                .containsExactlyInAnyOrder(file2.getOriginalFilename(), file3.getOriginalFilename());
    }

    @Test
    @DisplayName("상품 수정 실패, 상품이 존재 x/ 카테고리가 존재 x/ 올바른 판매자가 아닌 경우")
    void updateFail1() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String fileName4 = "test4.png";

        String contentType = "image/png";
        String productName = "상품이름";
        String info = "정보";
        LocalDateTime now = LocalDateTime.now();
        Long startPrice = 10000L;
        Long priceUnit = 1000L;
        LocalDateTime savedTime = now.plusDays(2);
        LocalDateTime updatedTime = now.plusMonths(1);
        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        MultipartFile file3 = new MockMultipartFile("testFile3", fileName4, contentType, "test4".getBytes());

        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));
        List<MultipartFile> updatedOrdinalFileList = new ArrayList<>(Arrays.asList(file2, file3));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member findMember1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member findMember2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category updateCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        ProductRegisterReq registerReq = new ProductRegisterReq(productName, info, findCategory.getId(), savedTime, startPrice, priceUnit, ordinalFileList, sigFile);

        // 상품 상태가 입찰 중이 아닌 경우(BID)
        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId1 = productService.register(registerDTO1);
        Product findProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct1.changeProductStatus(ProductStatus.TRANSACTION_FAIL);
        ProductUpdateReq productUpdateReq1 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice.intValue(), priceUnit.intValue(), updatedOrdinalFileList, sigFile);

        // 상품이 존재하지 않는 경우
        ProductRegisterDTO registerDTO2 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId2 = productService.register(registerDTO2);
        ProductUpdateReq productUpdateReq2 = new ProductUpdateReq(productName, info, -1L, updatedTime, startPrice.intValue(), priceUnit.intValue(), updatedOrdinalFileList, sigFile);

        // 올바른 판매자가 아닌 경우 (판매자가 아닌 사람이 수정하려 할 때)
        ProductRegisterDTO registerDTO3 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId3 = productService.register(registerDTO3);
        ProductUpdateReq productUpdateReq3 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice.intValue(), priceUnit.intValue(), updatedOrdinalFileList, sigFile);

        // 올바른 판매자가 아닌 경우 (상품 판매자는 맞지만 존재하지 않는 판매자(DELETED)일 경우)
        ProductRegisterDTO registerDTO4 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember2.getLoginId());
        Long savedId4 = productService.register(registerDTO4);
        ProductUpdateReq productUpdateReq4 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice.intValue(), priceUnit.intValue(), updatedOrdinalFileList, sigFile);
        findMember2.changeStatus(MemberStatus.DELETED);

        //then
        // 상품이 존재하지 않는 경우
        assertThatThrownBy(() ->productService.updateProduct(savedId1, productUpdateReq1, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.INVALID_UPDATE_PRODUCT_STATUS.getMessage());

        // 상품이 존재하지 않는 경우
        assertThatThrownBy(() ->productService.updateProduct(savedId2, productUpdateReq2, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());

        // 올바른 판매자가 아닌 경우 (판매자가 아닌 사람이 수정하려 할 때)
        assertThatThrownBy(() ->productService.updateProduct(savedId3, productUpdateReq3, findMember2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.INVALID_USER.getMessage());

        // 올바른 판매자가 아닌 경우 (상품 판매자는 맞지만 존재하지 않는 판매자(DELETED)일 경우)
        assertThatThrownBy(() ->productService.updateProduct(savedId4, productUpdateReq4, findMember2.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.INVALID_USER.getMessage());
    }

    @Test
    @DisplayName("상품 수정 실패, 상품 입찰 기록이 있는 경우")
    void updateFai2() throws Exception {
        //given
        // 사진 등록
        String fileName1 = "test1.png";
        String fileName2 = "test2.png";
        String fileName3 = "test3.png";
        String fileName4 = "test4.png";

        String contentType = "image/png";
        String productName = "상품이름";
        String info = "정보";
        LocalDateTime now = LocalDateTime.now();
        Long startPrice = 10000L;
        Long priceUnit = 1000L;
        LocalDateTime savedTime = now.plusDays(2);
        LocalDateTime updatedTime = now.plusMonths(1);
        MultipartFile sigFile = new MockMultipartFile("testFile1", fileName1, contentType, "test1".getBytes());
        MultipartFile file1 = new MockMultipartFile("testFile2", fileName2, contentType, "test2".getBytes());
        MultipartFile file2 = new MockMultipartFile("testFile3", fileName3, contentType, "test3".getBytes());
        MultipartFile file3 = new MockMultipartFile("testFile3", fileName4, contentType, "test4".getBytes());

        List<MultipartFile> ordinalFileList = new ArrayList<>(Arrays.asList(file1, file2));
        List<MultipartFile> updatedOrdinalFileList = new ArrayList<>(Arrays.asList(file2, file3));

        UploadFileDTO sigFileDTO = fileUploader.uploadFile(sigFile, FileSubPath.PRODUCT_IMG_PATH);
        List<UploadFileDTO> ordinalFileDTOList = fileUploader.uploadFiles(ordinalFileList, FileSubPath.PRODUCT_IMG_PATH);

        // 등록 dto 생성
        Member findMember1 = memberRepository.findByLoginId("20180584").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member findMember2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category findCategory = categoryRepository.findCategoryByName("생활/주방").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        Category updateCategory = categoryRepository.findCategoryByName("디지털기기").orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        ProductRegisterReq registerReq = new ProductRegisterReq(productName, info, findCategory.getId(), savedTime, startPrice, priceUnit, ordinalFileList, sigFile);

        // 상품 입찰 기록이 있는 경우
        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId1 = productService.register(registerDTO1);
        Product findProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        // 상품 입찰
        AuctionHistory auctionHistory = createAuctionHistory(findProduct1, 11000, findMember2);
        auctionHistoryRepository.save(auctionHistory);


        //then
        // 상품 입찰 기록이 있는 경우
        ProductUpdateReq productUpdateReq1 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice.intValue(), priceUnit.intValue(), updatedOrdinalFileList, sigFile);
        assertThatThrownBy(() ->productService.updateProduct(savedId1, productUpdateReq1, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.INVALID_UPDATE_PRODUCT_HISTORY.getMessage());


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