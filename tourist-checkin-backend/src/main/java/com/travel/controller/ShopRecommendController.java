package com.travel.controller;

import com.travel.dto.ShopRecommendCreateDTO;
import com.travel.service.ShopRecommendService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import com.travel.vo.ShopRecommendVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop-recommend")
@RequiredArgsConstructor
public class ShopRecommendController {

    private final ShopRecommendService shopRecommendService;

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }

    // ==================== User Endpoints ====================

    @PostMapping
    public Result<ShopRecommendVO> submit(@RequestBody @Valid ShopRecommendCreateDTO dto,
                                           HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(shopRecommendService.submit(userId, dto));
    }

    @GetMapping("/mine")
    public Result<Map<String, Object>> mine(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(shopRecommendService.mine(userId, page, size));
    }

    @GetMapping("/nearby")
    public Result<List<ShopRecommendVO>> nearby(@RequestParam double lat,
                                                 @RequestParam double lng,
                                                 @RequestParam(defaultValue = "5000") int radius,
                                                 @RequestParam(required = false) String category,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(shopRecommendService.nearby(lat, lng, radius, category, page, size));
    }

    @GetMapping("/{id}")
    public Result<ShopRecommendVO> detail(@PathVariable Long id) {
        return Result.success(shopRecommendService.detail(id));
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        shopRecommendService.like(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<ShopRecommendVO> resubmit(@PathVariable Long id,
                                             @RequestBody @Valid ShopRecommendCreateDTO dto,
                                             HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(shopRecommendService.resubmit(userId, id, dto));
    }

    // ==================== Admin Endpoints ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(shopRecommendService.adminList(page, size, status, keyword));
    }

    @PutMapping("/admin/{id}/audit")
    public Result<Void> adminAudit(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Long auditorId = requireUserId(request);
        if (body.get("status") == null) {
            throw new com.travel.exception.BadRequestException("缺少 status 参数");
        }
        int status = Integer.parseInt(body.get("status").toString());
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        shopRecommendService.adminAudit(id, auditorId, status, reason);
        return Result.success();
    }

    @PutMapping("/admin/{id}/feature")
    public Result<Void> adminFeature(@PathVariable Long id,
                                      @RequestParam int toggle) {
        shopRecommendService.adminFeature(id, toggle);
        return Result.success();
    }

    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> adminStats() {
        return Result.success(shopRecommendService.adminStats());
    }
}
