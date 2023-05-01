package com.auction.usedauction.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setData(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    public void setData(String key, String value, Long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void addList(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void setList(String key, List<String> list, Long timeout, TimeUnit unit) {
        for (String value : list) {
            addList(key, value);
        }
        redisTemplate.expire(key, timeout, unit);
    }

    public List<String> getList(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return redisTemplate.opsForList().range(key, 0, size).stream()
                .map(o -> (String) o).collect(Collectors.toList());
    }

    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
