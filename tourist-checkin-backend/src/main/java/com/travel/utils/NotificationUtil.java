package com.travel.utils;

import com.travel.entity.Notification;
import com.travel.mapper.NotificationMapper;
import com.travel.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtil {

    private final NotificationMapper notificationMapper;
    private final WebSocketService webSocketService;

    public void createAndPush(Long userId, Long fromUserId, String type,
                              String targetType, Long targetId, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setContent(content);
        notificationMapper.insert(notification);

        // WebSocket 实时推送
        webSocketService.pushToUser(userId, notification);
    }
}
