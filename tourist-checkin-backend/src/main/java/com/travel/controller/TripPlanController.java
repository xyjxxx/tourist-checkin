package com.travel.controller;

import com.travel.dto.TripPlanCreateDTO;
import com.travel.service.TripPlanService;
import com.travel.vo.Result;
import com.travel.vo.TripPlanBriefVO;
import com.travel.vo.TripPlanVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-plan")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping
    public Result<TripPlanVO> create(@RequestBody @Valid TripPlanCreateDTO dto,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(tripPlanService.create(userId, dto));
    }

    @GetMapping("/{id}")
    public Result<TripPlanVO> detail(@PathVariable Long id) {
        return Result.success(tripPlanService.getDetail(id));
    }

    @GetMapping("/my")
    public Result<List<TripPlanBriefVO>> myPlans(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(tripPlanService.listByUser(userId));
    }

    @GetMapping("/public")
    public Result<List<TripPlanBriefVO>> publicPlans(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(tripPlanService.listPublic(page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        tripPlanService.delete(userId, id);
        return Result.success();
    }
}
