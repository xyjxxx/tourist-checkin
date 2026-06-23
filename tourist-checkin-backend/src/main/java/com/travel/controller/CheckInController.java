package com.travel.controller;

import com.travel.dto.CheckInDTO;
import com.travel.service.CheckInService;
import com.travel.utils.AuthUtil;
import com.travel.vo.CheckInStatsVO;
import com.travel.vo.CheckInVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }

    @PostMapping
    public Result<Long> createCheckIn(@RequestBody @Valid CheckInDTO dto, HttpServletRequest request) {
        Long userId = requireUserId(request);
        Long id = checkInService.createCheckIn(userId, dto);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    public Result<CheckInVO> getCheckInDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = AuthUtil.getOptionalUserId(request);
        CheckInVO vo = checkInService.getDetailById(id, userId);
        if (vo == null) {
            throw new com.travel.exception.BadRequestException("打卡记录不存在");
        }
        return Result.success(vo);
    }

    @GetMapping("/my")
    public Result<List<CheckInVO>> listMyCheckIns(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(checkInService.listByUserId(userId, userId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<CheckInVO>> listUserCheckIns(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = requireUserId(request);
        return Result.success(checkInService.listByUserId(userId, currentUserId));
    }

    @GetMapping("/liked")
    public Result<List<CheckInVO>> getLikedCheckIns(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(checkInService.getLikedCheckIns(userId));
    }

    @GetMapping("/recent")
    public Result<List<CheckInVO>> listRecent(HttpServletRequest request) {
        Long userId = AuthUtil.getOptionalUserId(request);
        return Result.success(checkInService.listRecent(userId));
    }

    @GetMapping("/following")
    public Result<List<CheckInVO>> listFollowing(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(checkInService.listFollowing(userId, page, size));
    }

    @GetMapping("/hot")
    public Result<List<CheckInVO>> listHotThisMonth(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = AuthUtil.getOptionalUserId(request);
        return Result.success(checkInService.listHotThisMonth(userId, limit));
    }

    @PostMapping("/{checkInId}/like")
    public Result<Void> likeCheckIn(@PathVariable Long checkInId, HttpServletRequest request) {
        Long userId = requireUserId(request);
        checkInService.likeCheckIn(userId, checkInId);
        return Result.success();
    }

    @DeleteMapping("/{checkInId}")
    public Result<Void> deleteCheckIn(@PathVariable Long checkInId, HttpServletRequest request) {
        Long userId = requireUserId(request);
        checkInService.deleteCheckIn(userId, checkInId);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/all")
    public Result<List<CheckInVO>> getAllCheckIns(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(checkInService.getAllCheckIns(userId));
    }

    @GetMapping("/admin/page")
    public Result<Map<String, Object>> getCheckInPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        requireUserId(request);
        return Result.success(checkInService.getCheckInPage(keyword, page, size));
    }

    @DeleteMapping("/admin/{checkInId}")
    public Result<Void> adminDeleteCheckIn(@PathVariable Long checkInId, HttpServletRequest request) {
        Long userId = requireUserId(request);
        checkInService.adminDeleteCheckIn(userId, checkInId);
        return Result.success();
    }

    @GetMapping("/admin/stats")
    public Result<CheckInStatsVO> getCheckInStats(HttpServletRequest request) {
        requireUserId(request);
        return Result.success(checkInService.getCheckInStats());
    }
}
