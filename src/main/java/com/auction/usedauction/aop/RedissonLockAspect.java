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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;


@Aspect
@Component
@Order(1)
@Slf4j
public class RedissonLockAspect {

    private final RedissonClient redissonClient;
    private final TransactionTemplate txTemplate;

    public RedissonLockAspect(RedissonClient redissonClient, PlatformTransactionManager transactionManager) {
        this.redissonClient = redissonClient;
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    @Around("@annotation(redissonLock) && execution(* com.auction.usedauction..*(..))")
    public Object doLock(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        log.info("redisson Lock key = {}, isActiveTransaction = {}", redissonLock.key(), TransactionSynchronizationManager.isActualTransactionActive());
        RLock lock = redissonClient.getLock(redissonLock.key().name());
        try {
            log.info("redisson Lock 획득 시도, isActiveTransaction = {}", TransactionSynchronizationManager.isActualTransactionActive());
            boolean available = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), redissonLock.timeUnit());
            if (!available) { // 락 획득 실패
                CustomException customException = new CustomException(LockErrorCode.TRY_AGAIN_LOCK);
                log.error("redisson Lock 획득 실패, isActiveTransaction = {}", TransactionSynchronizationManager.isActualTransactionActive());
                throw customException;
            }
            log.info("redisson Lock 획득 성공, isActiveTransaction = {}", TransactionSynchronizationManager.isActualTransactionActive());

            return joinPoint.proceed();
        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for lock ", e);
            Thread.currentThread().interrupt();
            throw new CustomException(LockErrorCode.TRY_AGAIN_LOCK);

        } finally {
            log.info("redisson Lock 해제 시도, isActiveTransaction = {}", TransactionSynchronizationManager.isActualTransactionActive());
            lock.unlock();
            log.info("redisson Lock 해제 성공, isActiveTransaction = {}", TransactionSynchronizationManager.isActualTransactionActive());
            //IllegalMonitorStateException, transaction timeout 처리 추가해야 함
        }
    }
}
