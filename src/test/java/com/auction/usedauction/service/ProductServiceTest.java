package com.auction.usedauction.service;

import com.auction.usedauction.domain.*;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.*;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.AuctionRegisterDTO;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.service.dto.ProductUpdateReq;
import com.auction.usedauction.util.s3.FileSubPath;
import com.auction.usedauction.util.s3.S3FileUploader;
import com.auction.usedauction.util.s3.UploadFileDTO;
import com.auction.usedauction.web.dto.ProductRegisterReq;
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
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private S3FileUploader fileUploader;
    @Autowired
    private AuctionHistoryRepository auctionHistoryRepository;

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
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        //when
        Long savedId = productService.register(productRegisterDTO,auctionRegisterDTO);
        Product findProduct = productRepository.findById(savedId)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        //then
        //기본 정보
        assertThat(findProduct.getName()).isEqualTo(productRegisterDTO.getName());
        assertThat(findProduct.getInfo()).isEqualTo(productRegisterDTO.getInfo());
        assertThat(findProduct.getAuction().getStartPrice()).isEqualTo(auctionRegisterDTO.getStartPrice());
        assertThat(findProduct.getAuction().getNowPrice()).isEqualTo(auctionRegisterDTO.getStartPrice());

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
        ProductRegisterReq registerReq1 = new ProductRegisterReq("상품이름", "정보", findCategory1.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);
        ProductRegisterDTO productRegisterDTO1 = new ProductRegisterDTO(registerReq1, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO1 = new AuctionRegisterDTO(registerReq1.getAuctionEndDate(), registerReq1.getStartPrice(), registerReq1.getPriceUnit());

        // 카테고리가 존재하지 않는 경우
        Member findMember2 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        ProductRegisterReq registerReq2 = new ProductRegisterReq("상품이름", "정보", -1L, LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);
        ProductRegisterDTO productRegisterDTO2 = new ProductRegisterDTO(registerReq2, sigFileDTO, ordinalFileDTOList, findMember2.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO2 = new AuctionRegisterDTO(registerReq2.getAuctionEndDate(), registerReq2.getStartPrice(), registerReq2.getPriceUnit());

        //then
        // 판매자가 존재하지 않는 경우
        assertThatThrownBy(() -> productService.register(productRegisterDTO1,auctionRegisterDTO1))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());

        // 카테고리가 존재하지 않는 경우
        assertThatThrownBy(() -> productService.register(productRegisterDTO2,auctionRegisterDTO2))
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
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        Long savedId = productService.register(productRegisterDTO,auctionRegisterDTO);

        //when
        Long deletedId = productService.deleteProduct(savedId, findMember.getLoginId());

        //then
        Product findProduct1 = productRepository.findByIdAndProductStatus(deletedId, ProductStatus.EXIST).orElse(null);
        assertThat(findProduct1).isNull();

        Product findProduct2 = productRepository.findById(deletedId).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        assertThat(findProduct2.getProductStatus()).isEqualTo(ProductStatus.DELETED);

    }

    @Test
    @DisplayName("상품 삭제 실패, 상품이 존재하지 않는 경우")
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
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());

        // 상품 저장
        Long savedId1 = productService.register(productRegisterDTO,auctionRegisterDTO);

        // 상품 상태 변경
        Product savedProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        savedProduct1.changeProductStatus(ProductStatus.DELETED);

        //then
        // 상품 상태가 DELETED인 경우
        assertThatThrownBy(() -> productService.deleteProduct(savedId1, findMember.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        // 상품이 db에 존재하지 않는 경우
        assertThatThrownBy(() -> productService.deleteProduct(-1L, findMember.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 삭제 실패, 입찰상태일 때 입찰 기록이 있는 경우/입찰 상태가 아닌 경우")
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
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());

        // 입찰중일 때 입찰 기록이 있는 경우
        // 상품 저장 및 상품 입찰
        Long savedId1 = productService.register(productRegisterDTO,auctionRegisterDTO);
        Product savedProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Member buyer = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        AuctionHistory auctionHistory = createAuctionHistory(savedProduct1.getAuction(), 11000, buyer);
        auctionHistoryRepository.save(auctionHistory);

        //낙찰 상태인 경우
        Long savedId2 = productService.register(productRegisterDTO,auctionRegisterDTO);
        Product savedProduct2 = productRepository.findById(savedId2).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        savedProduct2.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);

        //then
        // 입찰중일 때 입찰 기록이 있는 경우
        assertThatThrownBy(() -> productService.deleteProduct(savedId1, findMember.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.EXIST_AUCTION_HISTORY.getMessage());
        // 입찰 상태가 아닌 경우
        assertThatThrownBy(() -> productService.deleteProduct(savedId2, findMember.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_DELETE_AUCTION_STATUS.getMessage());
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
        ProductRegisterReq registerReq = new ProductRegisterReq("상품이름", "정보", findCategory.getId(), LocalDateTime.now().plusDays(2), 10000, 1000, ordinalFileList, sigFile);

        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, seller1.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        // 상품 저장
        Long savedId1 = productService.register(productRegisterDTO,auctionRegisterDTO);

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
        int startPrice = 10000;
        int priceUnit = 1000;
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
        ProductRegisterDTO productRegisterDTO = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember.getLoginId());
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        Long savedId = productService.register(productRegisterDTO,auctionRegisterDTO);


        //when
        ProductUpdateReq productUpdateReq = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);
        Long updatedId = productService.updateProduct(savedId, productUpdateReq, findMember.getLoginId());

        //then
        Product findProduct = productRepository.findById(updatedId).
                orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        assertThat(findProduct.getName()).isEqualTo(productName);
        assertThat(findProduct.getInfo()).isEqualTo(info);
        assertThat(findProduct.getCategory()).isSameAs(updateCategory);
        assertThat(findProduct.getAuction().getAuctionEndDate()).isEqualTo(updatedTime);
        assertThat(findProduct.getAuction().getStartPrice()).isEqualTo(startPrice);
        assertThat(findProduct.getAuction().getPriceUnit()).isEqualTo(priceUnit);

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
        int startPrice = 10000;
        int priceUnit = 1000;
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
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());

        // 상품이 존재하지 않는 경우
        ProductRegisterDTO registerDTO1 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId1 = productService.register(registerDTO1,auctionRegisterDTO);
        Product findProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct1.changeProductStatus(ProductStatus.DELETED);
        ProductUpdateReq productUpdateReq1 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);

        // 존재하는 카테고리가 아닌 경우
        ProductRegisterDTO registerDTO2 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId2 = productService.register(registerDTO2,auctionRegisterDTO);
        ProductUpdateReq productUpdateReq2 = new ProductUpdateReq(productName, info, -1L, updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);

        // 올바른 판매자가 아닌 경우 (판매자가 아닌 사람이 수정하려 할 때)
        ProductRegisterDTO registerDTO3 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId3 = productService.register(registerDTO3,auctionRegisterDTO);
        ProductUpdateReq productUpdateReq3 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);

        // 올바른 판매자가 아닌 경우 (상품 판매자는 맞지만 존재하지 않는 판매자(DELETED)일 경우)
        ProductRegisterDTO registerDTO4 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember2.getLoginId());
        Long savedId4 = productService.register(registerDTO4,auctionRegisterDTO);
        ProductUpdateReq productUpdateReq4 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);
        findMember2.changeStatus(MemberStatus.DELETED);

        //then
        // 상품이 존재하지 않는 경우
        assertThatThrownBy(() ->productService.updateProduct(savedId1, productUpdateReq1, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());

        // 존재하는 카테고리가 아닌 경우
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
    @DisplayName("상품 수정 실패, 입찰기록이 있는 경우/입찰 상태가 아닌 다른 상태인 경우")
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
        int startPrice = 10000;
        int priceUnit = 1000;
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
        AuctionRegisterDTO auctionRegisterDTO = new AuctionRegisterDTO(registerReq.getAuctionEndDate(), registerReq.getStartPrice(), registerReq.getPriceUnit());
        Long savedId1 = productService.register(registerDTO1,auctionRegisterDTO);
        Product findProduct1 = productRepository.findById(savedId1).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        // 상품 입찰
        AuctionHistory auctionHistory = createAuctionHistory(findProduct1.getAuction(), 11000, findMember2);
        auctionHistoryRepository.save(auctionHistory);

        // 입찰 상태가 아닌 다른 상태인 경우
        // 낙찰 성공
        ProductRegisterDTO registerDTO2 = new ProductRegisterDTO(registerReq, sigFileDTO, ordinalFileDTOList, findMember1.getLoginId());
        Long savedId2 = productService.register(registerDTO2,auctionRegisterDTO);
        Product findProduct2 = productRepository.findById(savedId2).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct2.getAuction().changeAuctionStatus(AuctionStatus.SUCCESS_BID);
        // 거래 성공
        Long savedId3 = productService.register(registerDTO2,auctionRegisterDTO);
        Product findProduct3 = productRepository.findById(savedId3).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        findProduct3.getAuction().changeAuctionStatus(AuctionStatus.TRANSACTION_OK);

        //then
        // 상품 입찰 기록이 있는 경우
        ProductUpdateReq productUpdateReq1 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);
        assertThatThrownBy(() ->productService.updateProduct(savedId1, productUpdateReq1, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionHistoryErrorCode.EXIST_AUCTION_HISTORY.getMessage());

        // 입찰 상태가 아닌 다른 상태인 경우
        // 낙찰 성공
        ProductUpdateReq productUpdateReq2 = new ProductUpdateReq(productName, info, updateCategory.getId(), updatedTime, startPrice, priceUnit, updatedOrdinalFileList, sigFile);
        assertThatThrownBy(() ->productService.updateProduct(savedId2, productUpdateReq2, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_UPDATE_AUCTION_STATUS.getMessage());
        // 거래 성공
        assertThatThrownBy(() ->productService.updateProduct(savedId3, productUpdateReq2, findMember1.getLoginId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(AuctionErrorCode.INVALID_UPDATE_AUCTION_STATUS.getMessage());
    }

    private AuctionHistory createAuctionHistory(Auction auction, int bidPrice, Member member) {
        return AuctionHistory.builder()
                .auction(auction)
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