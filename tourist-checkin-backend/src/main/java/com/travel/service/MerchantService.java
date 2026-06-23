package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.MerchantPosition;
import com.travel.mapper.MerchantPositionMapper;
import com.travel.vo.MerchantPositionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantPositionMapper merchantPositionMapper;

    public List<MerchantPositionVO> searchNearby(double lat, double lng, double radius, String category,
                                                  int page, int size) {
        if (page < 1) page = 1;
        int limit = page * size;
        List<MerchantPosition> list = merchantPositionMapper.selectNearby(lat, lng, radius, limit);
        int offset = (page - 1) * size;
        if (list.size() > offset) {
            int to = Math.min(offset + size, list.size());
            return list.subList(offset, to).stream().map(this::convertToVO).toList();
        }
        return List.of();
    }

    public List<MerchantPositionVO> listByCategory(String category, int page, int size) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<MerchantPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantPosition::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MerchantPosition::getCategory, category);
        }
        wrapper.orderByDesc(MerchantPosition::getRating);
        wrapper.last("LIMIT " + ((page - 1) * size) + ", " + size);

        return merchantPositionMapper.selectList(wrapper).stream()
                .map(this::convertToVO).toList();
    }

    public MerchantPositionVO detail(Long id) {
        MerchantPosition m = merchantPositionMapper.selectById(id);
        if (m == null) {
            throw new com.travel.exception.BadRequestException("商户不存在");
        }
        return convertToVO(m);
    }

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminList(int page, int size, String keyword, String category) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<MerchantPosition> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(MerchantPosition::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MerchantPosition::getCategory, category);
        }
        wrapper.orderByDesc(MerchantPosition::getCreatedAt);
        Page<MerchantPosition> p = new Page<>(page, size);
        Page<MerchantPosition> result = merchantPositionMapper.selectPage(p, wrapper);
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("list", result.getRecords().stream().map(this::convertToVO).toList());
        map.put("total", result.getTotal());
        return map;
    }

    public MerchantPositionVO adminCreate(MerchantPosition data) {
        data.setStatus(1);
        merchantPositionMapper.insert(data);
        return convertToVO(data);
    }

    public void adminUpdate(Long id, MerchantPosition data) {
        MerchantPosition m = merchantPositionMapper.selectById(id);
        if (m == null) throw new com.travel.exception.BadRequestException("商户不存在");
        if (data.getName() != null) m.setName(data.getName());
        if (data.getCategory() != null) m.setCategory(data.getCategory());
        if (data.getAddress() != null) m.setAddress(data.getAddress());
        if (data.getLongitude() != null) m.setLongitude(data.getLongitude());
        if (data.getLatitude() != null) m.setLatitude(data.getLatitude());
        if (data.getCity() != null) m.setCity(data.getCity());
        if (data.getRating() != null) m.setRating(data.getRating());
        if (data.getPriceLevel() != null) m.setPriceLevel(data.getPriceLevel());
        if (data.getCoverImage() != null) m.setCoverImage(data.getCoverImage());
        if (data.getPhone() != null) m.setPhone(data.getPhone());
        if (data.getBusinessHours() != null) m.setBusinessHours(data.getBusinessHours());
        if (data.getDescription() != null) m.setDescription(data.getDescription());
        if (data.getStatus() != null) m.setStatus(data.getStatus());
        merchantPositionMapper.updateById(m);
    }

    @Transactional
    public void adminDelete(Long id) {
        merchantPositionMapper.deleteById(id);
    }

    private MerchantPositionVO convertToVO(MerchantPosition m) {
        MerchantPositionVO vo = new MerchantPositionVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setCategory(m.getCategory());
        vo.setAddress(m.getAddress());
        vo.setLongitude(m.getLongitude());
        vo.setLatitude(m.getLatitude());
        vo.setRating(m.getRating() != null ? m.getRating().doubleValue() : 0.0);
        vo.setPriceLevel(m.getPriceLevel());
        vo.setTags(m.getTags() != null && !m.getTags().isEmpty()
                ? Arrays.asList(m.getTags().split(",")) : List.of());
        vo.setCoverImage(m.getCoverImage());
        vo.setPhone(m.getPhone());
        vo.setBusinessHours(m.getBusinessHours());
        vo.setDescription(m.getDescription());
        return vo;
    }
}
