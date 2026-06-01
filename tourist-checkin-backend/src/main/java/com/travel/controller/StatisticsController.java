package com.travel.controller;

import com.travel.service.StatisticsService;
import com.travel.vo.ChartVO;
import com.travel.vo.Result;
import com.travel.vo.UserReportVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/report")
    public Result<UserReportVO> report(@RequestParam(defaultValue = "2026") int year,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(statisticsService.getUserReport(userId, year));
    }

    @GetMapping("/trend")
    public Result<ChartVO> trend(@RequestParam(defaultValue = "2026") int year,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(statisticsService.getMonthlyTrend(userId, year));
    }
}
