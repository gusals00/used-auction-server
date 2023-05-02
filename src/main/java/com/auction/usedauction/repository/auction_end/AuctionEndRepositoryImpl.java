package com.auction.usedauction.repository.auction_end;

import com.auction.usedauction.repository.dto.AuctionIdAndAuctionEndDateDTO;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@NoArgsConstructor
@Component
public class AuctionEndRepositoryImpl implements AuctionEndRepository {

    // key -> auctionId, value : auctionEndDate
    private final Map<Long, LocalDateTime> auctionEndMap = new ConcurrentHashMap<>();

    @Override
    public Long add(Long auctionId, LocalDateTime localDateTime) {
        auctionEndMap.put(auctionId, localDateTime);
        return auctionId;
    }

    @Override
    public void add(List<AuctionIdAndAuctionEndDateDTO> idAndAuctionEndDateDTOList) {
        for (AuctionIdAndAuctionEndDateDTO idAndAuctionEndDate : idAndAuctionEndDateDTOList) {
            add(idAndAuctionEndDate.getAuctionId(), idAndAuctionEndDate.getEndDate());
        }
    }

    @Override
    public LocalDateTime findByAuctionId(Long auctionId) {
        return auctionEndMap.get(auctionId);
    }

    @Override
    public void clearAll() {
        auctionEndMap.clear();
    }
}
