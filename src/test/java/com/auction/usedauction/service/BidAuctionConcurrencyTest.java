package com.auction.usedauction.service;

import com.auction.usedauction.domain.AuctionHistory;
import com.auction.usedauction.domain.Member;
import com.auction.usedauction.domain.Product;
import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.ProductErrorCode;
import com.auction.usedauction.exception.error_code.UserErrorCode;
import com.auction.usedauction.repository.CategoryRepository;
import com.auction.usedauction.repository.MemberRepository;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import com.auction.usedauction.repository.product.ProductRepository;
import com.auction.usedauction.repository.query.AuctionHistoryQueryRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SpringBootTest
@Slf4j
@Sql(value = {"/sql/concurrencyTest.sql"})
@Sql(value = {"/sql/concurrencyClean.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BidAuctionConcurrencyTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private AuctionHistoryService auctionHistoryService;
    @Autowired
    private AuctionHistoryRepository auctionHistoryRepository;
    @Autowired
    private AuctionHistoryQueryRepository auctionHistoryQueryRepository;

    @Test
    @Transactional()
    void concurrencyTest() throws Exception{
        //given
        Product product = productRepository.findByName("상품1").orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        Long auctionId = product.getAuction().getId();
        Member member1 = memberRepository.findByLoginId("20180012").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Member member2 = memberRepository.findByLoginId("20180592").orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        List<Member> buyerList = new ArrayList<>(List.of(member1,member2));

        int threadCnt = 2;
        int bidPrice = 16000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);
        CountDownLatch latch = new CountDownLatch(threadCnt);

        //when
        //동시 입찰
        for (int i = 0; i < threadCnt; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    auctionHistoryService.biddingAuction(auctionId,bidPrice,buyerList.get(index).getLoginId());
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    private class ParticipateWorker2 implements Runnable {
        private Long auctionId;
        private CountDownLatch countDownLatch;

        public ParticipateWorker2(Long auctionId, CountDownLatch countDownLatch) {
            this.auctionId = auctionId;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                log.info("wwwwwwwwwwww {}", auctionRepository.findById(auctionId).get().getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
