package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.entity.Notification;
import com.travel.mapper.NotificationMapper;
import com.travel.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public List<NotificationVO> page(Long userId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getStatus, 1)
               .orderByDesc(Notification::getCreatedAt);

        Page<Notification> pageParam = new Page<>(page, size);
        Page<Notification> result = notificationMapper.selectPage(pageParam, wrapper);

        return result.getRecords().stream().map(this::convertToVO).toList();
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

    private NotificationVO convertToVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTargetType(n.getTargetType());
        vo.setTargetId(n.getTargetId());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead() == 1);
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
