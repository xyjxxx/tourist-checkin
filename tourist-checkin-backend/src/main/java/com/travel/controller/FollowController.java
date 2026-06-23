package com.travel.controller;

import com.travel.service.FollowService;
import com.travel.utils.AuthUtil;
import com.travel.vo.FollowVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public Result<Void> follow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = requireUserId(request);
        followService.follow(currentUserId, userId);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = requireUserId(request);
        followService.unfollow(currentUserId, userId);
        return Result.success();
    }

    @GetMapping("/check/{userId}")
    public Result<Boolean> checkFollow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = requireUserId(request);
        return Result.success(followService.isFollowing(currentUserId, userId));
    }

    @GetMapping("/followers")
    public Result<List<FollowVO>> followers(@RequestParam Long userId) {
        return Result.success(followService.getFollowers(userId));
    }

    @GetMapping("/following")
    public Result<List<FollowVO>> following(@RequestParam Long userId) {
        return Result.success(followService.getFollowing(userId));
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
