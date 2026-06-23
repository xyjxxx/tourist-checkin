package com.travel.controller;

import com.travel.service.NotificationService;
import com.travel.utils.AuthUtil;
import com.travel.vo.NotificationVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/page")
    public Result<List<NotificationVO>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(notificationService.page(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        notificationService.markRead(userId, id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(HttpServletRequest request) {
        Long userId = requireUserId(request);
        notificationService.markAllRead(userId);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(notificationService.adminList(page, size));
    }

    @PostMapping("/admin/send")
    public Result<Void> adminSend(@RequestBody Map<String, Object> params) {
        Object targetObj = params.get("targetUserId");
        if (targetObj == null) {
            return Result.error(400, "缺少 targetUserId 参数");
        }
        Long targetUserId;
        try {
            targetUserId = Long.valueOf(targetObj.toString());
        } catch (NumberFormatException e) {
            throw new com.travel.exception.BadRequestException("targetUserId 格式不正确");
        }
        String content = (String) params.get("content");
        if (content == null || content.isBlank()) {
            return Result.error(400, "通知内容不能为空");
        }
        notificationService.adminSend(targetUserId, content);
        return Result.success();
    }

    @PostMapping("/admin/broadcast")
    public Result<Void> adminBroadcast(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new com.travel.exception.BadRequestException("广播内容不能为空");
        }
        notificationService.adminBroadcast(content);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
