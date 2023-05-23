package com.auction.usedauction.repository.file;

import com.auction.usedauction.domain.file.ProductVideo;

import java.util.Optional;

public interface ProductVideoRepositoryCustom {

    Optional<ProductVideo> findByProductVideoFetch(Long videoId);
}
