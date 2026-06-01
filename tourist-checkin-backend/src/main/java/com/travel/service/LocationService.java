package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.Location;
import com.travel.mapper.LocationMapper;
import com.travel.vo.LocationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationMapper locationMapper;

    // 粗略估算：1度 ≈ 111公里
    private static final double DEGREE_PER_KM = 1.0 / 111.0;

    public List<LocationVO> findNearby(BigDecimal longitude, BigDecimal latitude, Integer radiusInMeters) {
        double degree = radiusInMeters / 1000.0 * DEGREE_PER_KM;

        BigDecimal minLng = longitude.subtract(BigDecimal.valueOf(degree));
        BigDecimal maxLng = longitude.add(BigDecimal.valueOf(degree));
        BigDecimal minLat = latitude.subtract(BigDecimal.valueOf(degree));
        BigDecimal maxLat = latitude.add(BigDecimal.valueOf(degree));

        return locationMapper.selectNearby(minLng, maxLng, minLat, maxLat);
    }

    public List<LocationVO> findByCity(String city) {
        return locationMapper.selectByCity(city);
    }

    public List<LocationVO> listAll() {
        LambdaQueryWrapper<Location> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Location::getCreatedAt);
        wrapper.last("LIMIT 100");
        List<Location> list = locationMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).toList();
    }

    public LocationVO getById(Long id) {
        Location location = locationMapper.selectById(id);
        if (location == null) {
            return null;
        }
        return convertToVO(location);
    }

    private LocationVO convertToVO(Location location) {
        LocationVO vo = new LocationVO();
        BeanUtils.copyProperties(location, vo);
        return vo;
    }
}
