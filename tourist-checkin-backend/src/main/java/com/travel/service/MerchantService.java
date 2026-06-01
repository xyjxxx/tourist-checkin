package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.MerchantPosition;
import com.travel.mapper.MerchantPositionMapper;
import com.travel.vo.MerchantPositionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantPositionMapper merchantPositionMapper;

    public List<MerchantPositionVO> searchNearby(double lat, double lng, double radius, String category,
                                                  int page, int size) {
        LambdaQueryWrapper<MerchantPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantPosition::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MerchantPosition::getCategory, category);
        }
        int offset = (page - 1) * size;
        List<MerchantPosition> list = merchantPositionMapper.selectNearby(lat, lng, radius, size);
        if (list.size() > offset) {
            int to = Math.min(offset + size, list.size());
            return list.subList(offset, to).stream().map(this::convertToVO).toList();
        }
        return List.of();
    }

    public List<MerchantPositionVO> listByCategory(String category, int page, int size) {
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
            return null;
        }
        return convertToVO(m);
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
