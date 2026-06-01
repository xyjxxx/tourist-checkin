package com.travel.controller;

import com.travel.service.LocationService;
import com.travel.vo.LocationVO;
import com.travel.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/nearby")
    public Result<List<LocationVO>> nearby(
            @RequestParam BigDecimal lng,
            @RequestParam BigDecimal lat,
            @RequestParam(defaultValue = "5000") Integer radius) {
        return Result.success(locationService.findNearby(lng, lat, radius));
    }

    @GetMapping("/city/{city}")
    public Result<List<LocationVO>> listByCity(@PathVariable String city) {
        return Result.success(locationService.findByCity(city));
    }

    @GetMapping
    public Result<List<LocationVO>> listAll() {
        return Result.success(locationService.listAll());
    }

    @GetMapping("/{id}")
    public Result<LocationVO> getById(@PathVariable Long id) {
        LocationVO vo = locationService.getById(id);
        if (vo == null) {
            return Result.error("地点不存在");
        }
        return Result.success(vo);
    }
}
