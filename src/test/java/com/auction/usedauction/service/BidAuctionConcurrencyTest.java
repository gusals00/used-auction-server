package com.auction.usedauction.service;

import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.AuctionErrorCode;
import com.auction.usedauction.repository.auction.AuctionRepository;
import com.auction.usedauction.repository.auction_history.AuctionHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Slf4j
@Sql(value = {"/sql/concurrencyTest.sql"})
@Sql(value = {"/sql/concurrencyClean.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BidAuctionConcurrencyTest {

    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private AuctionHistoryService auctionHistoryService;
    @Autowired
    private AuctionHistoryRepository auctionHistoryRepository;

    @Test
    @DisplayName("동시에 2명이 같은 상품에 같은 가격으로 입찰하는 경우")
    @Transactional
    void concurrencyTest() throws Exception {
        //given
        List<String> buyerList = new ArrayList<>(List.of("20180012", "20180592"));
        int threadCnt = 2;
        int bidPrice = 15000;
        Long auctionId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);
        CountDownLatch latch = new CountDownLatch(threadCnt);

        //when
        //동시 입찰
        for (int i = 0; i < threadCnt; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    auctionHistoryService.biddingAuction(auctionId, bidPrice, buyerList.get(index));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        //then
        //현재가와 입찰가 비교
        int nowPrice = auctionRepository.findById(auctionId).orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND)).getNowPrice();
        assertThat(nowPrice).isEqualTo(bidPrice);

        //입찰내역 확인
        assertThat(auctionHistoryRepository.findAll().size()).isEqualTo(1);
        assertThat(auctionHistoryRepository.findAll().get(0).getBidPrice()).isEqualTo(bidPrice);
    }

    @Test
    @DisplayName("동시에 여러명이 같은 상품에 같은 가격으로 입찰하는 경우")
    @Transactional
    void concurrencyTest2() throws Exception {
        //given
        List<String> buyerList = new ArrayList<>(List.of("20180012", "20180592", "20180004", "20180211"));
        List<Integer> bidPricelist = new ArrayList<>(List.of( 15000, 17000, 21000,16000));

        int loop = 100;
        int threadCnt = buyerList.size();
        Long auctionId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt*loop);
        CountDownLatch latch = new CountDownLatch(loop*threadCnt);

        //when
        //동시 입찰
        for (int i = 0; i < loop; i++) {
            for (int j = 0; j < threadCnt; j++) {
                int index = j;
                executorService.submit(() -> {
                    try {
                        auctionHistoryService.biddingAuction(auctionId, bidPricelist.get(index), buyerList.get(index));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        latch.await();

        //then
        //현재가와 입찰가 비교
        int nowPrice = auctionRepository.findById(auctionId).orElseThrow(() -> new CustomException(AuctionErrorCode.AUCTION_NOT_FOUND)).getNowPrice();
        assertThat(nowPrice).isEqualTo(21000);

    }
}
