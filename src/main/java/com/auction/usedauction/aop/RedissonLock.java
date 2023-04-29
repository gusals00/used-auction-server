package com.auction.usedauction.aop;

import com.auction.usedauction.util.LockKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedissonLock {

    long waitTime() default 3;

    long leaseTime() default 3;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    LockKey key();
}
