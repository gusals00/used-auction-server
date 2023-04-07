package com.auction.usedauction.repository;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, String> {

    List<AuctionHistory> findAllByProduct(Product product);
}
