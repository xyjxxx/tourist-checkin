package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.Location;
import com.travel.mapper.LocationMapper;
import com.travel.vo.LocationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationMapper locationMapper;

    // 1度纬度 ≈ 111公里
    private static final double KM_PER_DEGREE_LAT = 111.0;

    public List<LocationVO> findNearby(BigDecimal longitude, BigDecimal latitude, Integer radiusInMeters) {
        double radiusKm = radiusInMeters / 1000.0;
        double deltaLat = radiusKm / KM_PER_DEGREE_LAT;
        // 经度度数随纬度变化：1度经度 = cos(纬度) * 111km
        double latRad = Math.toRadians(latitude.doubleValue());
        double cosLat = Math.cos(latRad);
        if (Math.abs(latitude.doubleValue()) >= 89.99) {
            cosLat = Math.cos(Math.toRadians(89.99));
        }
        double deltaLng = radiusKm / (KM_PER_DEGREE_LAT * cosLat);

        BigDecimal minLng = longitude.subtract(BigDecimal.valueOf(deltaLng));
        BigDecimal maxLng = longitude.add(BigDecimal.valueOf(deltaLng));
        BigDecimal minLat = latitude.subtract(BigDecimal.valueOf(deltaLat));
        BigDecimal maxLat = latitude.add(BigDecimal.valueOf(deltaLat));

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

    // ==================== 管理员功能 ====================

    public LocationVO adminCreate(Location data) {
        locationMapper.insert(data);
        return convertToVO(data);
    }

    public void adminUpdate(Long id, Location data) {
        Location loc = locationMapper.selectById(id);
        if (loc == null) throw new com.travel.exception.BadRequestException("地点不存在");
        if (data.getName() != null) loc.setName(data.getName());
        if (data.getAddress() != null) loc.setAddress(data.getAddress());
        if (data.getLongitude() != null) loc.setLongitude(data.getLongitude());
        if (data.getLatitude() != null) loc.setLatitude(data.getLatitude());
        if (data.getCategory() != null) loc.setCategory(data.getCategory());
        if (data.getCity() != null) loc.setCity(data.getCity());
        if (data.getDescription() != null) loc.setDescription(data.getDescription());
        if (data.getCoverImage() != null) loc.setCoverImage(data.getCoverImage());
        locationMapper.updateById(loc);
    }

    @Transactional
    public void adminDelete(Long id) {
        Location loc = new Location();
        loc.setId(id);
        loc.setDeleted(1);
        locationMapper.updateById(loc);
    }

    private LocationVO convertToVO(Location location) {
        LocationVO vo = new LocationVO();
        BeanUtils.copyProperties(location, vo);
        return vo;
    }
}
