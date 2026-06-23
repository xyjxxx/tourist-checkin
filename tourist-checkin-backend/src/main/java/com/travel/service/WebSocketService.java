package com.travel.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
public class WebSocketService {

    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void registerSession(Long userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(session);
            if (userSessions.isEmpty()) {
                sessions.remove(userId);
            }
        }
    }

    public void pushToUser(Long userId, Object message) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) return;
        String json = JSON.toJSONString(message);
        Iterator<WebSocketSession> it = userSessions.iterator();
        while (it.hasNext()) {
            WebSocketSession session = it.next();
            if (!session.isOpen()) {
                it.remove();
                continue;
            }
            try {
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("WebSocket 推送失败: userId={}", userId, e);
            }
        }
    }
}
