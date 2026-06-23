package com.travel.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private final StringRedisTemplate redisTemplate;

    private static final String LUA_SCRIPT =
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
            "return current";

    /**
     * 限流检查：指定时间窗口内允许的最大请求数（原子操作）
     * @param key       限流键（如 IP 或用户名）
     * @param limit     最大请求数
     * @param window    时间窗口
     * @param failClose Redis 故障时是否拒绝请求（true=安全端点应拒绝，false=普通端点可放行）
     * @return true=允许请求, false=被限流
     */
    public boolean isAllowed(String key, int limit, Duration window, boolean failClose) {
        try {
            String redisKey = "rate_limit:" + key;
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
            Long count = redisTemplate.execute(script, Collections.singletonList(redisKey),
                    String.valueOf(window.getSeconds()));
            return count != null && count <= limit;
        } catch (Exception e) {
            if (failClose) {
                log.error("限流检查失败（安全模式），拒绝请求: {}", e.getMessage());
                return false;
            }
            log.warn("限流检查失败，放行请求: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 限流检查（默认 fail-open，兼容旧调用）
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        return isAllowed(key, limit, window, false);
    }
}
