package com.travel.controller;

import com.travel.service.MerchantService;
import com.travel.vo.MerchantPositionVO;
import com.travel.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.travel.entity.MerchantPosition;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/nearby")
    public Result<List<MerchantPositionVO>> nearby(@RequestParam double lat,
                                                    @RequestParam double lng,
                                                    @RequestParam(defaultValue = "5000") double radius,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.success(merchantService.searchNearby(lat, lng, radius, category, page, size));
    }

    @GetMapping("/category")
    public Result<List<MerchantPositionVO>> byCategory(@RequestParam(required = false) String category,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(merchantService.listByCategory(category, page, size));
    }

    @GetMapping("/{id}")
    public Result<MerchantPositionVO> detail(@PathVariable Long id) {
        return Result.success(merchantService.detail(id));
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(merchantService.adminList(page, size, keyword, category));
    }

    @PostMapping("/admin")
    public Result<MerchantPositionVO> adminCreate(@RequestBody MerchantPosition data) {
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            return Result.error("商户名称不能为空");
        }
        data.setId(null);
        data.setCreatedAt(null);
        return Result.success(merchantService.adminCreate(data));
    }

    @PutMapping("/admin/{id}")
    public Result<Void> adminUpdate(@PathVariable Long id, @RequestBody MerchantPosition data) {
        merchantService.adminUpdate(id, data);
        return Result.success();
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        merchantService.adminDelete(id);
        return Result.success();
    }
}
