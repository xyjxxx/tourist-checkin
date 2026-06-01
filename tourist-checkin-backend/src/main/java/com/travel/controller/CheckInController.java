package com.travel.controller;

import com.travel.dto.CheckInDTO;
import com.travel.service.CheckInService;
import com.travel.vo.CheckInStatsVO;
import com.travel.vo.CheckInVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public Result<Long> createCheckIn(@RequestBody @Valid CheckInDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = checkInService.createCheckIn(userId, dto);
        return Result.success(id);
    }

    @GetMapping("/my")
    public Result<List<CheckInVO>> listMyCheckIns(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.listByUserId(userId, userId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<CheckInVO>> listUserCheckIns(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.listByUserId(userId, currentUserId));
    }

    /**
     * 获取当前用户点赞的打卡记录
     */
    @GetMapping("/liked")
    public Result<List<CheckInVO>> getLikedCheckIns(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.getLikedCheckIns(userId));
    }

    @GetMapping("/recent")
    public Result<List<CheckInVO>> listRecent(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.listRecent(userId));
    }

    @PostMapping("/{checkInId}/like")
    public Result<Void> likeCheckIn(@PathVariable Long checkInId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkInService.likeCheckIn(userId, checkInId);
        return Result.success();
    }

    @DeleteMapping("/{checkInId}")
    public Result<Void> deleteCheckIn(@PathVariable Long checkInId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkInService.deleteCheckIn(userId, checkInId);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/all")
    public Result<List<CheckInVO>> getAllCheckIns(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(checkInService.getAllCheckIns(userId));
    }

    @DeleteMapping("/admin/{checkInId}")
    public Result<Void> adminDeleteCheckIn(@PathVariable Long checkInId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkInService.adminDeleteCheckIn(userId, checkInId);
        return Result.success();
    }

    @GetMapping("/admin/stats")
    public Result<CheckInStatsVO> getCheckInStats() {
        return Result.success(checkInService.getCheckInStats());
    }
}
