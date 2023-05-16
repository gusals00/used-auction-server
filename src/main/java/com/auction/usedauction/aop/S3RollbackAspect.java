package com.auction.usedauction.aop;

import com.auction.usedauction.util.S3BackUpManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class S3RollbackAspect {

    private final S3BackUpManager backUpManager;

    @Around("@annotation(s3Rollback) && execution(* com.auction.usedauction..*(..))")
    public Object s3BackUp(ProceedingJoinPoint joinPoint, S3Rollback s3Rollback) throws Throwable {
        try {
            backUpManager.begin();
            Object result = joinPoint.proceed();
            backUpManager.end();
            return result;
        } catch (Exception e) {
            backUpManager.backUp();
            throw e;
        }
    }
}
