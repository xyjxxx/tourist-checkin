package com.travel.controller;

import com.travel.service.PointService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import com.travel.vo.UserPointVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/my")
    public Result<UserPointVO> myPoints(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(pointService.getUserPoints(userId));
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> records(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(pointService.getRecords(userId, page, size));
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
