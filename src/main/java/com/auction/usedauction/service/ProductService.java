package com.auction.usedauction.service;

import com.auction.usedauction.aop.S3Rollback;
import com.auction.usedauction.domain.*;
import com.auction.usedauction.domain.file.File;
import com.auction.usedauction.domain.file.ProductImage;
import com.auction.usedauction.domain.file.ProductImageType;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.*;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction_end.AuctionEndRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.file.FileRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.AuctionRegisterDTO;
import com.auction.usedauction.service.dto.ProductRegisterDTO;
import com.auction.usedauction.service.dto.ProductUpdateReq;
import com.auction.usedauction.util.FileSubPath;
import com.auction.usedauction.util.S3FileUploader;
import com.auction.usedauction.util.UploadFileDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.auction.usedauction.domain.ProductStatus.*;
import static java.util.stream.Collectors.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final FileRepository fileRepository;
    private final S3FileUploader fileUploader;
    private final AuctionRepository auctionRepository;
    private final AuctionEndRepository auctionEndRepository;

    @Transactional
    public Long register(ProductRegisterDTO productRegisterDTO, AuctionRegisterDTO auctionRegisterDTO) {

        // 판매자 존재 체크
        Member member = memberRepository.findOneWithAuthoritiesByLoginIdAndStatus(productRegisterDTO.getLoginId(), MemberStatus.EXIST)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 카테고리 존재 체크
        Category category = categoryRepository.findById(productRegisterDTO.getCategoryId())
                .orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //대표 사진 생성
        ProductImage productSigImage = createProductImage(productRegisterDTO.getSigProductImg(), ProductImageType.SIGNATURE);
        //일반 사진 생성
        List<ProductImage> productOrdinalImageList = createProductImageList(productRegisterDTO.getOrdinalProductImg(), ProductImageType.ORDINAL);

        // 경매 저장
        Auction auction = createAuction(auctionRegisterDTO.getStartPrice(), auctionRegisterDTO.getPriceUnit(), auctionRegisterDTO.getAuctionEndDate());
        auctionRepository.save(auction);

        //상품 저장
        Product product = createProduct(productRegisterDTO, productSigImage, productOrdinalImageList, member, category, auction);
        productRepository.save(product);

        log.info("상품 등록 성공 productId={},sellerId={}, sigImgId={}, ordinalImgIds={}",
                product.getId(), member.getId(), productSigImage.getId(), getOrdinalImagesIdsToString(productOrdinalImageList));
        return product.getId();
    }

    private Auction createAuction(int startPrice, int priceUnit, LocalDateTime auctionEndDate) {
        return Auction.builder()
                .auctionEndDate(auctionEndDate)
                .startPrice(startPrice)
                .priceUnit(priceUnit)
                .build();
    }

    @Transactional
    public Long deleteProduct(Long productId, String loginId) {

        // 상품이 존재하는지
        Product findProduct = productRepository.findByIdAndProductStatus(productId, EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 경매 상태가 상품을 삭제할 수 있는 상태인지
        Auction findAuction = findProduct.getAuction();
        isValidAuctionDeleteStatus(findAuction);

        // 상품 제거 권한 있는 판매자인지 + 탈퇴하지 않은 존재하는 판매자인지
        validRightSeller(findProduct, loginId);

        //상품 상태 DELETED(삭제)로 변경
        findProduct.changeProductStatus(DELETED);

        return findProduct.getId();
    }

    private void isValidAuctionUpdateStatus(Auction auction) {
        // 경매 상태가 입찰이 아닌 경우에는 상품 변경/삭제 불가능
        if (!isAuctionStatusEq(auction, AuctionStatus.BID)) {
            throw new CustomException(AuctionErrorCode.INVALID_UPDATE_AUCTION_STATUS);
        }

        //입찰 기록이 없는 경우만 가능
        if (hasAuctionHistoryWhenBidding(auction)) {
            throw new CustomException(AuctionHistoryErrorCode.EXIST_AUCTION_HISTORY);
        }
    }

    private void isValidAuctionDeleteStatus(Auction auction) {
        // 경매 상태가 입찰이 아닌 경우에는 상품 변경/삭제 불가능
        if (!isAuctionStatusEq(auction, AuctionStatus.BID)) {
            throw new CustomException(AuctionErrorCode.INVALID_DELETE_AUCTION_STATUS);
        }

        //입찰 기록이 없는 경우만 가능
        if (hasAuctionHistoryWhenBidding(auction)) {
            throw new CustomException(AuctionHistoryErrorCode.EXIST_AUCTION_HISTORY);
        }
    }

    //상품이 입찰 상태일 때 입찰 기록이 있는지
    private boolean hasAuctionHistoryWhenBidding(Auction auction) {
        return auction.getStatus() == AuctionStatus.BID && auctionHistoryRepository.countByAuction(auction) > 0;
    }

    private boolean isAuctionStatusEq(Auction auction, AuctionStatus status) {
        return auction.getStatus() == status;
    }

    @Transactional
    @S3Rollback
    public Long updateProduct(Long productId, ProductUpdateReq updateReq, String loginId) {

        //상품이 존재하는지 확인
        Product findProduct = productRepository.findByIdAndProductStatus(productId, EXIST)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 경매 상태가 상품을 수정할 수 있는 상태인지 확인
        Auction auction = findProduct.getAuction();
        isValidAuctionUpdateStatus(auction);

        // 카테고리 존재 확인
        Category category = categoryRepository.findById(updateReq.getCategoryId())
                .orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        //수정하려는 판매자가 올바른 판매자인지(상품 등록자가 맞는지 + 상품 등록자 상태가 EXIST 인지)
        validRightSeller(findProduct, loginId);

        //사진수정
        updateOrdinalImage(findProduct, updateReq.getImgList());
        updateSigImage(findProduct, updateReq.getSigImg());

        //상품 및 경매 정보 수정
        findProduct.changeProduct(updateReq.getProductName(), updateReq.getInfo(), category);
        auction.changeAuction(updateReq.getStartPrice(), updateReq.getPriceUnit(), updateReq.getAuctionEndDate());

        // 변경된 경매 종료 날짜가 현재시간 ~ 24시간 이내의 날짜일 경우 auctionEndRepository에 저장
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        if (updateReq.getAuctionEndDate().isBefore(endDate)) {
            auctionEndRepository.add(auction.getId(), updateReq.getAuctionEndDate());
            log.info("수정된 경매 종료 날짜가 24시간 이내의 날짜로 변경,  변경 날짜 : {}", updateReq.getAuctionEndDate());
        }
        return findProduct.getId();
    }

    private void updateOrdinalImage(Product product, List<MultipartFile> multipartFileList) {
        List<ProductImage> ordinalImageList = product.getOrdinalImageList();
        deleteImageForUpdate(ordinalImageList, multipartFileList, product);
        insertImageUpdate(ordinalImageList, multipartFileList, ProductImageType.ORDINAL, product);
    }

    private void updateSigImage(Product product, MultipartFile multipartFile) {

        List<ProductImage> ordinalImageList = new ArrayList<>(Arrays.asList(product.getSigImage()));
        List<MultipartFile> multipartFileList = new ArrayList<>(Arrays.asList(multipartFile));

        deleteImageForUpdate(ordinalImageList, multipartFileList, product);
        insertImageUpdate(ordinalImageList, multipartFileList, ProductImageType.SIGNATURE, product);
    }

    private void deleteImageForUpdate(List<ProductImage> productImageList, List<MultipartFile> multipartImages, Product product) {
        String[] inputOriginalNames = multipartImages.stream()
                .map(MultipartFile::getOriginalFilename)
                .toArray(String[]::new);

        //삭제해야 할 ProductImage 엔티티 찾기
        List<ProductImage> deleteImageEntityList = findDeleteImageEntityList(productImageList, inputOriginalNames);

        // 엔티티 삭제
        // 양방향 매핑되어 있어서 product 쪽에서도 FileList 에서 ProductImage 삭제하여 상태를 동기화 시킨 후 엔티티 삭제해야 함.
        product.getFileList().removeIf(deleteImageEntityList::contains);
        fileRepository.deleteAll(deleteImageEntityList);

        // s3 삭제
        fileUploader.deleteFiles(
                deleteImageEntityList
                        .stream()
                        .map(File::getPath)
                        .collect(toList())
        );
    }

    private void insertImageUpdate(List<ProductImage> productImageList, List<MultipartFile> multipartImages, ProductImageType imageType, Product product) {
        String[] productImageOriginalNames = productImageList.stream()
                .map(File::getOriginalName)
                .toArray(String[]::new);

        //추가해야 할 multipartFile 찾기
        List<MultipartFile> insertMultipartFiles = findInsertMultipartFiles(multipartImages, productImageOriginalNames);
        //사진 저장
        List<UploadFileDTO> uploadFIleDTOList = fileUploader.uploadFiles(insertMultipartFiles, FileSubPath.PRODUCT_IMG_PATH);

        //엔티티 생성
        List<ProductImage> createdProductImageList = uploadFIleDTOList.stream()
                .map(uploadFIle -> createProductImage(uploadFIle, imageType, product))
                .toList();
        //엔티티 저장
        fileRepository.saveAll(createdProductImageList);
    }

    //추가해야 할 multipartFile 찾기
    private List<MultipartFile> findInsertMultipartFiles(List<MultipartFile> multipartFileList, String[] productImageOriginalNames) {
        return multipartFileList.stream()
                .filter(multipartFile -> doesNotContain(multipartFile.getOriginalFilename(), productImageOriginalNames))
                .collect(toList());
    }

    //삭제해야 할 엔티티 찾기
    private List<ProductImage> findDeleteImageEntityList(List<ProductImage> productImageList, String[] multipartFileOriginalNames) {
        return productImageList.stream()
                .filter(productImage -> doesNotContain(productImage.getOriginalName(), multipartFileOriginalNames))
                .collect(toList());
    }

    private boolean doesNotContain(String target, String[] elements) {
        return !StringUtils.containsAny(target, elements);
    }


    private void validRightSeller(Product product, String loginId) {
        if (!isRightSeller(product, loginId)) {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
    }

    private boolean isRightSeller(Product product, String loginId) {
        return product.getMember().getLoginId().equals(loginId) && product.getMember().getStatus() == MemberStatus.EXIST;
    }

    public String getOrdinalImagesIdsToString(List<ProductImage> images) {
        return images.stream()
                .map(image -> String.valueOf(image.getId()))
                .toList()
                .toString();

    }

    private Product createProduct(ProductRegisterDTO productRegisterDTO, ProductImage productSigImage, List<ProductImage> productOrdinalImageList,
                                  Member member, Category category, Auction auction) {
        return Product.builder()
                .info(productRegisterDTO.getInfo())
                .sigImage(productSigImage)
                .ordinalImageList(productOrdinalImageList)
                .category(category)
                .member(member)
                .name(productRegisterDTO.getName())
                .auction(auction)
                .build();
    }


    private List<ProductImage> createProductImageList(List<UploadFileDTO> uploadImageList, ProductImageType imageType) {
        return uploadImageList.stream()
                .map(uploadFIleDTO -> createProductImage(uploadFIleDTO, imageType))
                .collect(toList());
    }

    private ProductImage createProductImage(UploadFileDTO uploadImage, ProductImageType imageType, Product product) {
        ProductImage productImage = ProductImage.builder()
                .originalName(uploadImage.getUploadFileName())
                .path(uploadImage.getStoreUrl())
                .fullPath(uploadImage.getStoreFullUrl())
                .type(imageType)
                .build();
        productImage.changeProduct(product);
        return productImage;
    }

    private ProductImage createProductImage(UploadFileDTO uploadImage, ProductImageType imageType) {
        return ProductImage.builder()
                .originalName(uploadImage.getUploadFileName())
                .path(uploadImage.getStoreUrl())
                .fullPath(uploadImage.getStoreFullUrl())
                .type(imageType)
                .build();
    }
}
