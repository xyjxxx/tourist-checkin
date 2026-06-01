package com.travel.controller;

import com.travel.dto.TravelNoteCreateDTO;
import com.travel.service.TravelNoteService;
import com.travel.vo.Result;
import com.travel.vo.TravelNoteVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-note")
@RequiredArgsConstructor
public class TravelNoteController {

    private final TravelNoteService travelNoteService;

    @PostMapping
    public Result<TravelNoteVO> create(@RequestBody @Valid TravelNoteCreateDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(travelNoteService.create(userId, dto));
    }

    @GetMapping("/{id}")
    public Result<TravelNoteVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
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
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(travelNoteService.listRecent(page, size));
    }

    /**
     * 获取当前用户点赞的游记
     */
    @GetMapping("/liked")
    public Result<List<TravelNoteVO>> liked(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(travelNoteService.getLikedTravelNotes(userId));
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        travelNoteService.like(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/collect")
    public Result<Void> collect(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        travelNoteService.collect(userId, id);
        return Result.success();
    }
}
