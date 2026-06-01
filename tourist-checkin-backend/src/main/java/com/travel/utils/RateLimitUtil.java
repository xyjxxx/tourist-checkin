package com.travel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private final StringRedisTemplate redisTemplate;

    /**
     * 滑动窗口限流：指定时间窗口内允许的最大请求数
     * @param key     限流键（如 IP 或用户名）
     * @param limit   最大请求数
     * @param window  时间窗口
     * @return true=允许请求, false=被限流
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        try {
            String redisKey = "rate_limit:" + key;
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, window);
            }
            return count != null && count <= limit;
        } catch (Exception e) {
            // Redis 不可用时放行，避免影响正常业务
            log.warn("限流检查失败，放行请求: {}", e.getMessage());
            return true;
        }
    }
}
