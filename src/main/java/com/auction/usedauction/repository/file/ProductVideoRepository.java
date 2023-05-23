package com.auction.usedauction.repository.file;

import com.auction.usedauction.domain.file.ProductVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVideoRepository extends JpaRepository<ProductVideo, Long>, ProductVideoRepositoryCustom {

}
