package com.travel.controller;

import com.travel.service.MerchantService;
import com.travel.vo.MerchantPositionVO;
import com.travel.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
