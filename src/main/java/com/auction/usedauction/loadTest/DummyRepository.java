package com.auction.usedauction.loadTest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DummyRepository extends JpaRepository<Dummy, Long> {

    @Query(value = "select distinct auction_id from dummy", nativeQuery = true)
    List<Long> findByDistinctBidAuctionId();
}
