package com.travel.controller;

import com.travel.service.PointService;
import com.travel.vo.PointRecordVO;
import com.travel.vo.Result;
import com.travel.vo.UserPointVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/my")
    public Result<UserPointVO> myPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(pointService.getUserPoints(userId));
    }

    @GetMapping("/records")
    public Result<List<PointRecordVO>> records(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(pointService.getRecords(userId, page, size));
    }
}
