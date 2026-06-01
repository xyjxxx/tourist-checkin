package com.travel.controller;

import com.travel.service.AchievementService;
import com.travel.vo.AchievementVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/my")
    public Result<List<AchievementVO>> myAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(achievementService.getUserAchievements(userId));
    }

    @GetMapping("/definitions")
    public Result<List<AchievementVO>> definitions() {
        return Result.success(achievementService.getAllDefinitions());
    }
}
