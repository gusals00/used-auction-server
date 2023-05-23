package com.auction.usedauction.repository.product;

import com.auction.usedauction.domain.Product;
import com.auction.usedauction.repository.dto.ProductSearchCondDTO;
import com.auction.usedauction.web.dto.MyPageSearchConReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface ProductRepositoryCustom {

    Page<Product> findBySearchCond(ProductSearchCondDTO searchCond, Pageable pageable);
    Optional<Product> findExistProductByIdAndExistMember(Long productId);
    Page<Product> findMyProductsByCond(String loginId, MyPageSearchConReq cond, Pageable pageable);
    Page<Product> findMySalesHistoryByCond(String loginId, MyPageSearchConReq cond, Pageable pageable);
    Integer findNowPriceByProductId(Long productId);
    boolean existProductByIdAndLoginId(Long productId, String loginId);
}
