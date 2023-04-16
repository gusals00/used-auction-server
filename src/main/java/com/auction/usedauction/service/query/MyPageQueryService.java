package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.dto.MyPageAuctionHistoryPageContentRes;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.*;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import com.auction.usedauction.web.dto.PageListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MyPageQueryService {

    private final ProductRepository productRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;

    // 상품 관리
    public PageListRes<MyPageProductPageContentRes> getMyProductPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = getPageRequest(myPageSearchConReq);

        Page<Product> findPage = productRepository.findMyProductsByCond(loginId, myPageSearchConReq, pageRequest);

        List<MyPageProductPageContentRes> myPageProductPageContents = findPage.getContent().stream()
                .map(MyPageProductPageContentRes::new)
                .toList();

        return new PageListRes(myPageProductPageContents, findPage);
    }

    // 구매 내역
    public PageListRes<MyPageBuySellHistoryContentRes> getMyBuyHistoryPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = getPageRequest(myPageSearchConReq);

        Page<Product> findPage = productRepository.findMyBuyHistoryByCond(loginId, myPageSearchConReq, pageRequest);

        List<MyPageBuySellHistoryContentRes> myPageBuyHistoryContents = findPage.getContent().stream()
                .map(MyPageBuySellHistoryContentRes::new)
                .toList();

        return new PageListRes(myPageBuyHistoryContents, findPage);
    }

    // 판매 내역
    public PageListRes<MyPageBuySellHistoryContentRes> getMySalesHistoryPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = getPageRequest(myPageSearchConReq);

        Page<Product> findPage = productRepository.findMySalesHistoryByCond(loginId, myPageSearchConReq, pageRequest);

        List<MyPageBuySellHistoryContentRes> myPageBuyHistoryContents = findPage.getContent().stream()
                .map(MyPageBuySellHistoryContentRes::new)
                .toList();

        return new PageListRes(myPageBuyHistoryContents, findPage);
    }

    // 입찰/낙찰 내역
    public PageListRes<MyPageAuctionHistoryPageContentRes> getMyAuctionHistoryPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = getPageRequest(myPageSearchConReq);

        Page<AuctionHistory> findPage = auctionHistoryRepository.findMyAuctionHistoryByCond(loginId, myPageSearchConReq, pageRequest);

        List<MyPageAuctionHistoryPageContentRes> myPageAuctionHistoryPageContents = findPage.getContent().stream()
                .map(MyPageAuctionHistoryPageContentRes::new)
                .toList();

        return new PageListRes(myPageAuctionHistoryPageContents, findPage);
    }

    private PageRequest getPageRequest(MyPageSearchConReq myPageSearchConReq) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());
        return pageRequest;
    }
}
