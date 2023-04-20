package com.auction.usedauction.service;

import com.auction.usedauction.domain.Auction;
import com.auction.usedauction.domain.AuctionStatus;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.domain.TransStatus;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.dto.SellerAndBuyerLoginIdDTO;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 낙찰 성공 상태 경매 조회
        Auction auction = auctionRepository.findAuctionByIdAndStatus(auctionId,AuctionStatus.SUCCESS_BID)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND));

        // 변경하려는 상태가 올바르지 않은 경우(TRANS_BEFORE)
        if (transStatus == TRANS_BEFORE) {
            throw new CustomException(AuctionErrorCode.INVALID_CHANGE_TRANS);
        }

        SellerAndBuyerLoginIdDTO sellerAndBuyerLoginId = auctionHistoryQueryRepository.findSellerAndBuyerLoginId(auctionId)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.INVALID_AUCTION));

        // 판매자 또는 구매자 TransStatus 변경
        changeValidMemberTransStatus(auction ,sellerAndBuyerLoginId, loginId,transStatus);

        // 판매자, 구매자 거래 상태 확인 후 경매 상태 변경
        changeAuctionTrans(auction);
    }

    private void changeValidMemberTransStatus(Auction auction,SellerAndBuyerLoginIdDTO loginIdsDTO, String loginId,TransStatus transStatus) {
        if (loginIdsDTO.getBuyerLoginId().equals(loginId)) {// 판매자일 경우
            log.info("구매자 TransStatus={} 로 변경",transStatus);
            auction.changeBuyerStatus(transStatus);
        } else if (loginIdsDTO.getSellerLoginId().equals(loginId)) {// 구매자일 경우
            log.info("판매자 TransStatus={} 로 변경",transStatus);
            auction.changeSellerStatus(transStatus);
        }else {
            throw new CustomException(UserErrorCode.INVALID_USER);
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
