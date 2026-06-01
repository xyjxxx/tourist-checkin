package com.travel.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketService {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        sessions.remove(userId, session);
    }

    public void pushToUser(Long userId, Object message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("WebSocket 推送失败: userId={}", userId, e);
            }
        }
    }
}
