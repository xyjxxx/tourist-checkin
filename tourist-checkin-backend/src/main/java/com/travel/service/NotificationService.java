package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.entity.Notification;
import com.travel.entity.User;
import com.travel.mapper.NotificationMapper;
import com.travel.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.travel.mapper.UserMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final TransactionTemplate transactionTemplate;

    public List<NotificationVO> page(Long userId, int page, int size) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getStatus, 1)
               .orderByDesc(Notification::getCreatedAt);

        Page<Notification> pageParam = new Page<>(page, size);
        Page<Notification> result = notificationMapper.selectPage(pageParam, wrapper);

        Map<Long, User> userMap = batchLoadFromUsers(result.getRecords());
        return result.getRecords().stream().map(n -> convertToVO(n, userMap)).toList();
    }

    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .eq(Notification::getStatus, 1);
        return notificationMapper.selectCount(wrapper);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminList(int page, int size) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getStatus, 1)
               .orderByDesc(Notification::getCreatedAt);
        Page<Notification> p = new Page<>(page, size);
        Page<Notification> result = notificationMapper.selectPage(p, wrapper);
        Map<String, Object> map = new java.util.HashMap<>();
        Map<Long, User> userMap = batchLoadFromUsers(result.getRecords());
        map.put("list", result.getRecords().stream().map(n -> convertToVO(n, userMap)).toList());
        map.put("total", result.getTotal());
        return map;
    }

    public void adminSend(Long targetUserId, String content) {
        Notification n = new Notification();
        n.setUserId(targetUserId);
        n.setType("SYSTEM");
        n.setContent(content);
        n.setIsRead(0);
        n.setStatus(1);
        notificationMapper.insert(n);
    }

    public void adminBroadcast(String content) {
        int pageNum = 1;
        int batchSize = 500;
        while (true) {
            Page<com.travel.entity.User> userPage = userMapper.selectPage(
                    new Page<>(pageNum, batchSize),
                    new LambdaQueryWrapper<com.travel.entity.User>().eq(com.travel.entity.User::getDeleted, 0));
            List<com.travel.entity.User> users = userPage.getRecords();
            if (users.isEmpty()) break;

            // 每批独立事务，避免长事务占用连接，使用批量INSERT减少SQL执行次数
            List<com.travel.entity.User> batch = users;
            transactionTemplate.executeWithoutResult(status -> {
                List<Notification> notifications = new java.util.ArrayList<>(batch.size());
                for (com.travel.entity.User user : batch) {
                    Notification n = new Notification();
                    n.setUserId(user.getId());
                    n.setType("SYSTEM");
                    n.setContent(content);
                    n.setIsRead(0);
                    n.setStatus(1);
                    notifications.add(n);
                }
                // 分批批量插入，每批200条
                int insertBatchSize = 200;
                for (int i = 0; i < notifications.size(); i += insertBatchSize) {
                    int end = Math.min(i + insertBatchSize, notifications.size());
                    notificationMapper.batchInsert(notifications.subList(i, end));
                }
            });

            if (users.size() < batchSize) break;
            pageNum++;
        }
    }

    private Map<Long, User> batchLoadFromUsers(List<Notification> notifications) {
        List<Long> fromUserIds = notifications.stream()
                .map(Notification::getFromUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (fromUserIds.isEmpty()) return Map.of();
        List<User> users = userMapper.selectBatchIds(fromUserIds);
        Map<Long, User> map = new HashMap<>(users.size());
        for (User u : users) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private NotificationVO convertToVO(Notification n) {
        Map<Long, User> userMap = n.getFromUserId() != null
                ? batchLoadFromUsers(List.of(n)) : Map.of();
        return convertToVO(n, userMap);
    }

    private NotificationVO convertToVO(Notification n, Map<Long, User> userMap) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTargetType(n.getTargetType());
        vo.setTargetId(n.getTargetId());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead() == 1);
        vo.setCreatedAt(n.getCreatedAt());
        // 填充触发者信息
        if (n.getFromUserId() != null) {
            User fromUser = userMap.get(n.getFromUserId());
            if (fromUser != null) {
                com.travel.vo.UserBriefVO brief = new com.travel.vo.UserBriefVO();
                brief.setId(fromUser.getId());
                brief.setUsername(fromUser.getUsername());
                brief.setAvatar(fromUser.getAvatar());
                vo.setFromUser(brief);
            }
        }
        return vo;
    }
}
