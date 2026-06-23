package com.travel.controller;

import com.travel.dto.TripPlanCreateDTO;
import com.travel.entity.TripPlan;
import com.travel.exception.BadRequestException;
import com.travel.mapper.TripPlanMapper;
import com.travel.service.TripPlanService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import com.travel.vo.TripPlanBriefVO;
import com.travel.vo.TripPlanVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trip-plan")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;
    private final TripPlanMapper tripPlanMapper;

    @PostMapping
    public Result<TripPlanVO> create(@RequestBody @Valid TripPlanCreateDTO dto,
                                      HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(tripPlanService.create(userId, dto));
    }

    @GetMapping("/{id}")
    public Result<TripPlanVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = AuthUtil.getOptionalUserId(request);
        TripPlanVO vo = tripPlanService.getDetail(id);
        if (Boolean.FALSE.equals(vo.getIsPublic())) {
            TripPlan plan = tripPlanMapper.selectById(id);
            if (plan != null && (userId == null || !userId.equals(plan.getUserId()))) {
                throw new BadRequestException("无权查看私密行程");
            }
        }
        return Result.success(vo);
    }

    @GetMapping("/my")
    public Result<List<TripPlanBriefVO>> myPlans(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(tripPlanService.listByUser(userId));
    }

    @GetMapping("/public")
    public Result<List<TripPlanBriefVO>> publicPlans(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(tripPlanService.listPublic(page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        tripPlanService.delete(userId, id);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(tripPlanService.adminList(page, size, keyword));
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        tripPlanService.adminDelete(id);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
