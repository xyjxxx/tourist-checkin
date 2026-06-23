package com.travel.controller;

import com.travel.service.StatisticsService;
import com.travel.utils.AuthUtil;
import com.travel.vo.ChartVO;
import com.travel.vo.Result;
import com.travel.vo.UserReportVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/report")
    public Result<UserReportVO> report(@RequestParam(required = false) Integer year,
                                        HttpServletRequest request) {
        if (year == null) year = java.time.LocalDate.now().getYear();
        Long userId = requireUserId(request);
        return Result.success(statisticsService.getUserReport(userId, year));
    }

    @GetMapping("/trend")
    public Result<ChartVO> trend(@RequestParam(required = false) Integer year,
                                  HttpServletRequest request) {
        if (year == null) year = java.time.LocalDate.now().getYear();
        Long userId = requireUserId(request);
        return Result.success(statisticsService.getMonthlyTrend(userId, year));
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/overview")
    public Result<Map<String, Object>> adminOverview(HttpServletRequest request) {
        requireUserId(request);
        return Result.success(statisticsService.adminOverview());
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
