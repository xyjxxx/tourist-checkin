package com.travel.utils;

import com.travel.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthUtil {

    private static JwtUtil staticJwtUtil;

    public AuthUtil(JwtUtil jwtUtil) {
        AuthUtil.staticJwtUtil = jwtUtil;
    }

    public static Long requireUserId(HttpServletRequest request) {
        // 优先从拦截器设置的 attribute 获取
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) return userId;

        // 拦截器未设置时，直接解析 token
        userId = parseUserIdFromToken(request);
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        return userId;
    }

    public static Long getOptionalUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) return userId;
        return parseUserIdFromToken(request);
    }

    private static Long parseUserIdFromToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring(7);
        try {
            if (staticJwtUtil != null && staticJwtUtil.validateToken(token)) {
                Long userId = staticJwtUtil.getUserIdFromToken(token);
                log.debug("AuthUtil: parsed userId={} from token", userId);
                return userId;
            }
        } catch (Exception e) {
            log.warn("AuthUtil: token parse failed: {}", e.getMessage());
        }
        return null;
    }
}
