package com.travel.controller;

import com.travel.service.AchievementService;
import com.travel.utils.AuthUtil;
import com.travel.vo.AchievementVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.travel.entity.Achievement;

import java.util.List;

@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/my")
    public Result<List<AchievementVO>> myAchievements(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(achievementService.getUserAchievements(userId));
    }

    @GetMapping("/definitions")
    public Result<List<AchievementVO>> definitions() {
        return Result.success(achievementService.getAllDefinitions());
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<List<AchievementVO>> adminList() {
        return Result.success(achievementService.getAllDefinitions());
    }

    @PostMapping("/admin")
    public Result<AchievementVO> adminCreate(@RequestBody Achievement achievement) {
        if (achievement.getName() == null || achievement.getName().trim().isEmpty()) {
            return Result.error("成就名称不能为空");
        }
        achievement.setId(null);
        achievement.setCreatedAt(null);
        return Result.success(achievementService.adminCreate(achievement));
    }

    @PutMapping("/admin/{id}")
    public Result<Void> adminUpdate(@PathVariable Long id, @RequestBody Achievement data) {
        achievementService.adminUpdate(id, data);
        return Result.success();
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        achievementService.adminDelete(id);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
