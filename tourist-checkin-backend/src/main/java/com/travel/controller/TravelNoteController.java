package com.travel.controller;

import com.travel.dto.TravelNoteCreateDTO;
import com.travel.service.TravelNoteService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import com.travel.vo.TravelNoteVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-note")
@RequiredArgsConstructor
public class TravelNoteController {

    private final TravelNoteService travelNoteService;

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }

    @PostMapping
    public Result<TravelNoteVO> create(@RequestBody @Valid TravelNoteCreateDTO dto,
                                        HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(travelNoteService.create(userId, dto));
    }

    @GetMapping("/{id}")
    public Result<TravelNoteVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = AuthUtil.getOptionalUserId(request);
        return Result.success(travelNoteService.detail(id, userId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<TravelNoteVO>> listByUser(@PathVariable Long userId) {
        return Result.success(travelNoteService.listByUser(userId));
    }

    @GetMapping("/hot")
    public Result<List<TravelNoteVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(travelNoteService.listHot(limit));
    }

    @GetMapping("/recent")
    public Result<List<TravelNoteVO>> recent(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(travelNoteService.listRecent(page, size, keyword));
    }

    /**
     * 获取当前用户点赞的游记
     */
    @GetMapping("/liked")
    public Result<List<TravelNoteVO>> liked(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(travelNoteService.getLikedTravelNotes(userId));
    }

    /**
     * 获取当前用户收藏的游记
     */
    @GetMapping("/collected")
    public Result<List<TravelNoteVO>> collected(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(travelNoteService.getCollectedTravelNotes(userId));
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        travelNoteService.like(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/collect")
    public Result<Void> collect(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        travelNoteService.collect(userId, id);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(travelNoteService.adminList(page, size, status, keyword));
    }

    @PutMapping("/admin/{id}/audit")
    public Result<Void> adminAudit(@PathVariable Long id, @RequestParam int status) {
        travelNoteService.adminAudit(id, status);
        return Result.success();
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        travelNoteService.adminDelete(id);
        return Result.success();
    }

    @PutMapping("/admin/{id}/pin")
    public Result<Void> adminTogglePin(@PathVariable Long id) {
        travelNoteService.adminTogglePin(id);
        return Result.success();
    }
}
