package com.auction.usedauction.aop;

import com.auction.usedauction.exception.CustomException;
import com.auction.usedauction.exception.error_code.LockErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Aspect
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(redissonLock) && execution(* com.auction.usedauction..*(..))")
    public Object doLock(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {

//        RLock lock = redissonClient.getLock(redissonLock.key().name());
        Long auctionId = (Long) joinPoint.getArgs()[0];
        RLock lock = redissonClient.getLock(redissonLock.key().name()+auctionId%3);
        try {
            boolean available = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), redissonLock.timeUnit());
            if (!available) { // 락 획득 실패
                CustomException customException = new CustomException(LockErrorCode.TRY_AGAIN_LOCK);
                throw customException;
            }

            return joinPoint.proceed();

        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for lock ", e);
            Thread.currentThread().interrupt();
            throw new CustomException(LockErrorCode.TRY_AGAIN_LOCK);

        } finally {
            log.info("redisson Lock 해제 시도");
            lock.unlock();
            log.info("redisson Lock 해제 성공");
        }
    }
}
