package com.auction.usedauction.repository.auction_history;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, String>, AuctionHistoryRepositoryCustom {

    List<AuctionHistory> findAllByProduct(Product product);
}
