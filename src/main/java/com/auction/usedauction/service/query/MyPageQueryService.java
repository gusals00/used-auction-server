package com.auction.usedauction.service.query;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.repository.product.ProductRepository;
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

    public MyPageProductPageRes getMyProductPage(MyPageSearchConReq myPageSearchConReq, String loginId) {
        PageRequest pageRequest = PageRequest.of(myPageSearchConReq.getPage(), myPageSearchConReq.getSize());

        Page<Product> findPage = productRepository.findMyProductsByCond(loginId, myPageSearchConReq, pageRequest);
        List<Product> contents = findPage.getContent();

        List<MyPageProductPageContentRes> myPageProductPageContents = contents.stream()
                .map(MyPageProductPageContentRes::new)
                .toList();

        return new MyPageProductPageRes(myPageProductPageContents, findPage);
    }
}
