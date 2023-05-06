package com.auction.usedauction.service;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.TransStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.dto.SellerAndBuyerLoginIdDTO;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.auction.usedauction.domain.TransStatus.*;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionHistoryQueryRepository auctionHistoryQueryRepository;


    // 거래 확정
    @Transactional
    public void memberTransConfirm(Long auctionId, String loginId, TransStatus transStatus) {
        // 낙찰성공/거래성공/거래실패 상태 경매 조회
        Auction auction = auctionRepository.findAuctionByIdAndStatusIn(auctionId,Arrays.asList(AuctionStatus.SUCCESS_BID,AuctionStatus.TRANSACTION_OK,AuctionStatus.TRANSACTION_FAIL))
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));

        // 변경하려는 상태가 올바르지 않은 경우(TRANS_BEFORE)
        if (transStatus == TRANS_BEFORE) {
            throw new CustomException(AuctionErrorCode.INVALID_CHANGE_TRANS);
        }

        SellerAndBuyerLoginIdDTO sellerAndBuyerLoginId = auctionHistoryQueryRepository.findSellerAndBuyerLoginId(auctionId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.INVALID_AUCTION));

        // 경매 상태 확인
        validAuctionStatus(auction.getStatus());

        // 판매자 또는 구매자 TransStatus 변경
        changeValidMemberTransStatus(auction ,sellerAndBuyerLoginId, loginId,transStatus);

        // 판매자, 구매자 거래 상태 확인 후 경매 상태 변경
        changeAuctionTrans(auction);
    }

    @Transactional
    public List<Long> changeBulkAuctionMemberTransAndAuctionStatus(LocalDateTime criteriaTime) {
        // 낙찰 후 1주일이 경과되었지만 거래 확정이 되지 않은 경매 id 조회
        List<Long> findAuctionIds = auctionRepository.findSuccessButNotTransIdByDate(criteriaTime);

        // 판매자,구매자 거래 상태가 TRANS_BEFORE 상태를 TRANS_COMPLETE 변경
        auctionRepository.updateAuctionBuyerTransStatus(TransStatus.TRANS_BEFORE, TransStatus.TRANS_COMPLETE, findAuctionIds);
        auctionRepository.updateAuctionSellerTransStatus(TransStatus.TRANS_BEFORE, TransStatus.TRANS_COMPLETE, findAuctionIds);
        // 거래 상태에 따른 경매 상태 변경
        auctionRepository.updateAuctionStatusByTransStatusConfirm(findAuctionIds);
        return findAuctionIds;
    }

    private void validAuctionStatus(AuctionStatus status) {
        // 이미 거래 확정이 종료된 경매인 경우
        if (status == AuctionStatus.TRANSACTION_FAIL || status == AuctionStatus.TRANSACTION_OK) {
            throw new CustomException(AuctionErrorCode.ALREADY_AUCTION_TRANS_COMPLETE);
        }
    }

    private void changeValidMemberTransStatus(Auction auction,SellerAndBuyerLoginIdDTO loginIdsDTO, String loginId,TransStatus transStatus) {
        if (loginIdsDTO.getBuyerLoginId().equals(loginId)) {// 판매자일 경우
            validRightUserTrans(auction.getBuyerTransStatus());
            log.info("구매자 TransStatus={} 로 변경",transStatus);
            auction.changeBuyerStatus(transStatus);
        } else if (loginIdsDTO.getSellerLoginId().equals(loginId)) {// 구매자일 경우
            validRightUserTrans(auction.getSellerTransStatus());
            log.info("판매자 TransStatus={} 로 변경",transStatus);
            auction.changeSellerStatus(transStatus);
        }else {
            throw new CustomException(UserErrorCode.INVALID_USER);
        }
    }

    private void validRightUserTrans(TransStatus transStatus) {
        if (transStatus != TRANS_BEFORE) {
            throw new CustomException(AuctionErrorCode.ALREADY_USER_CHANGE_TRANS);
        }
    }

    // 판매자, 구매자 거래 상태 확인 후 경매 상태 변경
    private void changeAuctionTrans(Auction auction) {

        TransStatus buyerTrans = auction.getBuyerTransStatus();
        TransStatus sellerTrans = auction.getSellerTransStatus();
        // 판매자, 구매자 모두 TRANS_BEFORE 상태가 아닌 경우
        if (buyerTrans != TRANS_BEFORE && sellerTrans != TRANS_BEFORE) {
            if (buyerTrans == TRANS_COMPLETE && sellerTrans == TRANS_COMPLETE) {
                auction.changeAuctionStatus(AuctionStatus.TRANSACTION_OK);
            } else {
                auction.changeAuctionStatus(AuctionStatus.TRANSACTION_FAIL);
            }
        }
    }

    private boolean isProductSeller(Product product, String loginId) {
        return product.getMember().getLoginId().equals(loginId);
    }

}
