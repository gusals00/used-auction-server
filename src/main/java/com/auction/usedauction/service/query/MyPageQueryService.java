package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.service.dto.MyPageAuctionHistoryPageContentRes;
import com.auction.usedauction.service.dto.MyPageAuctionHistoryPageRes;
import com.auction.usedauction.service.dto.MyPageProductPageContentRes;
import com.auction.usedauction.service.dto.MyPageProductPageRes;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
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

    public MyPageProductPageRes getMyProductPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());

        Page<Product> findPage = productRepository.findMyProductsByCond(loginId, myPageSearchConReq, pageRequest);
        List<Product> contents = findPage.getContent();

        List<MyPageProductPageContentRes> myPageProductPageContents = contents.stream()
                .map(MyPageProductPageContentRes::new)
                .toList();

        return new MyPageProductPageRes(myPageProductPageContents, findPage);
    }

    public MyPageAuctionHistoryPageRes getMyAuctionHistory(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());

        Page<AuctionHistory> findPage = auctionHistoryRepository.findMyAuctionHistoryByCond(loginId, myPageSearchConReq, pageRequest);
        List<AuctionHistory> contents = findPage.getContent();

        List<MyPageAuctionHistoryPageContentRes> myPageAuctionHistoryPageContents = contents.stream()
                .map(MyPageAuctionHistoryPageContentRes::new)
                .toList();

        return new MyPageAuctionHistoryPageRes(myPageAuctionHistoryPageContents, findPage);
    }

}
