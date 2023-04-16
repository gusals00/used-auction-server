package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.Product;
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

    // 상품 관리
    public PageListRes<MyPageProductPageContentRes> getMyProductPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());

        Page<Product> findPage = productRepository.findMyProductsByCond(loginId, myPageSearchConReq, pageRequest);
        List<Product> contents = findPage.getContent();

        List<MyPageProductPageContentRes> myPageProductPageContents = contents.stream()
                .map(MyPageProductPageContentRes::new)
                .toList();

        return new PageListRes(myPageProductPageContents, findPage);
    }

    // 구매 내역
    public PageListRes<MyPageBuySellHistoryContentRes> getMyBuyHistoryPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());

        Page<Product> findPage = productRepository.findMyBuyHistoryByCond(loginId, myPageSearchConReq, pageRequest);
        List<Product> contents = findPage.getContent();

        List<MyPageBuySellHistoryContentRes> myPageBuyHistoryContents = contents.stream()
                .map(MyPageBuySellHistoryContentRes::new)
                .toList();

        return new PageListRes(myPageBuyHistoryContents, findPage);
    }

    // 판매 내역
    public PageListRes<MyPageBuySellHistoryContentRes> getMySalesHistoryPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());

        Page<Product> findPage = productRepository.findMySalesHistoryByCond(loginId, myPageSearchConReq, pageRequest);
        List<Product> contents = findPage.getContent();

        List<MyPageBuySellHistoryContentRes> myPageBuyHistoryContents = contents.stream()
                .map(MyPageBuySellHistoryContentRes::new)
                .toList();

        return new PageListRes(myPageBuyHistoryContents, findPage);
    }
}
