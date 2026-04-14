package com.loan.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 */
public class RedisLockUtil {

    private static StringRedisTemplate redisTemplate;

    public static void setRedisTemplate(StringRedisTemplate template) {
        redisTemplate = template;
    }

    /**
     * 尝试获取锁
     */
    public static boolean tryLock(String key, String value, long timeout) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放锁
     */
    public static void unlock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 检查key是否存在
     */
    public static boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置值
     */
    public static void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取值
     */
    public static String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
