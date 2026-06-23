package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.TripDayDTO;
import com.travel.dto.TripPlanCreateDTO;
import com.travel.dto.TripPOIDTO;
import com.travel.entity.TripDay;
import com.travel.entity.TripPlan;
import com.travel.entity.TripPOI;
import com.travel.exception.BadRequestException;
import com.travel.exception.UnauthorizedException;
import com.travel.mapper.TripDayMapper;
import com.travel.mapper.TripPlanMapper;
import com.travel.mapper.TripPOIMapper;
import com.travel.vo.TripDayVO;
import com.travel.vo.TripPlanBriefVO;
import com.travel.vo.TripPlanVO;
import com.travel.vo.TripPOIVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlanService {

    private final TripPlanMapper tripPlanMapper;
    private final TripDayMapper tripDayMapper;
    private final TripPOIMapper tripPOIMapper;

    @Transactional
    public TripPlanVO create(Long userId, TripPlanCreateDTO dto) {
        TripPlan plan = new TripPlan();
        plan.setUserId(userId);
        plan.setTitle(dto.getTitle());
        plan.setDescription(dto.getDescription());
        plan.setCity(dto.getCity());
        plan.setCoverImage(dto.getCoverImage());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setTotalDays(dto.getDays() != null ? dto.getDays().size() : 0);
        plan.setIsPublic(Boolean.TRUE.equals(dto.getIsPublic()) ? 1 : 0);
        plan.setStatus(1);
        tripPlanMapper.insert(plan);

        if (dto.getDays() != null) {
            for (TripDayDTO dayDTO : dto.getDays()) {
                TripDay day = new TripDay();
                day.setPlanId(plan.getId());
                day.setDayNumber(dayDTO.getDayNumber());
                day.setTitle(dayDTO.getTitle());
                day.setDate(dayDTO.getDate());
                tripDayMapper.insert(day);

                if (dayDTO.getPois() != null) {
                    int sort = 0;
                    for (TripPOIDTO poiDTO : dayDTO.getPois()) {
                        TripPOI poi = new TripPOI();
                        poi.setDayId(day.getId());
                        poi.setName(poiDTO.getName());
                        poi.setAddress(poiDTO.getAddress());
                        poi.setLongitude(poiDTO.getLongitude());
                        poi.setLatitude(poiDTO.getLatitude());
                        poi.setCategory(poiDTO.getCategory());
                        poi.setDurationMinutes(poiDTO.getDurationMinutes());
                        poi.setNotes(poiDTO.getNotes());
                        poi.setSortOrder(sort++);
                        tripPOIMapper.insert(poi);
                    }
                }
            }
        }

        return getDetail(plan.getId());
    }

    public TripPlanVO getDetail(Long planId) {
        TripPlan plan = tripPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BadRequestException("行程不存在");
        }
        return convertToVO(plan);
    }

    public List<TripPlanBriefVO> listByUser(Long userId) {
        return tripPlanMapper.selectByUserId(userId).stream()
                .map(this::convertToBriefVO).toList();
    }

    public List<TripPlanBriefVO> listPublic(int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        return tripPlanMapper.selectPublic(offset, size).stream()
                .map(this::convertToBriefVO).toList();
    }

    @Transactional
    public void delete(Long userId, Long planId) {
        TripPlan plan = tripPlanMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权删除");
        }
        plan.setStatus(0);
        tripPlanMapper.updateById(plan);
    }

    private TripPlanVO convertToVO(TripPlan plan) {
        TripPlanVO vo = new TripPlanVO();
        vo.setId(plan.getId());
        vo.setTitle(plan.getTitle());
        vo.setDescription(plan.getDescription());
        vo.setCity(plan.getCity());
        vo.setCoverImage(plan.getCoverImage());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setTotalDays(plan.getTotalDays());
        vo.setIsPublic(plan.getIsPublic() == 1);
        vo.setStatus(plan.getStatus());
        vo.setCreatedAt(plan.getCreatedAt());

        // 批量加载天数（2条SQL代替1+N条）
        List<TripDay> days = tripDayMapper.selectByPlanId(plan.getId());
        List<Long> dayIds = days.stream().map(TripDay::getId).toList();
        Map<Long, List<TripPOI>> poisByDay = dayIds.isEmpty() ? Map.of() :
                tripPOIMapper.selectByDayIds(dayIds).stream()
                        .collect(Collectors.groupingBy(TripPOI::getDayId));

        vo.setDays(days.stream().map(day -> {
            TripDayVO dayVO = new TripDayVO();
            dayVO.setId(day.getId());
            dayVO.setDayNumber(day.getDayNumber());
            dayVO.setTitle(day.getTitle());
            dayVO.setDate(day.getDate());

            dayVO.setPois(poisByDay.getOrDefault(day.getId(), List.of()).stream().map(poi -> {
                TripPOIVO poiVO = new TripPOIVO();
                poiVO.setId(poi.getId());
                poiVO.setName(poi.getName());
                poiVO.setAddress(poi.getAddress());
                poiVO.setLongitude(poi.getLongitude());
                poiVO.setLatitude(poi.getLatitude());
                poiVO.setCategory(poi.getCategory());
                poiVO.setDurationMinutes(poi.getDurationMinutes());
                poiVO.setNotes(poi.getNotes());
                poiVO.setSortOrder(poi.getSortOrder());
                return poiVO;
            }).toList());
            return dayVO;
        }).toList());

        return vo;
    }

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminList(int page, int size, String keyword) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<TripPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(TripPlan::getStatus, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TripPlan::getTitle, keyword).or().like(TripPlan::getCity, keyword));
        }
        wrapper.orderByDesc(TripPlan::getCreatedAt);
        Page<TripPlan> p = new Page<>(page, size);
        Page<TripPlan> result = tripPlanMapper.selectPage(p, wrapper);

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("list", result.getRecords().stream().map(this::convertToBriefVO).toList());
        map.put("total", result.getTotal());
        return map;
    }

    public void adminDelete(Long planId) {
        TripPlan plan = tripPlanMapper.selectById(planId);
        if (plan == null) throw new BadRequestException("行程不存在");
        plan.setStatus(0);
        tripPlanMapper.updateById(plan);
    }

    private TripPlanBriefVO convertToBriefVO(TripPlan plan) {
        TripPlanBriefVO vo = new TripPlanBriefVO();
        vo.setId(plan.getId());
        vo.setTitle(plan.getTitle());
        vo.setDescription(plan.getDescription());
        vo.setCity(plan.getCity());
        vo.setCoverImage(plan.getCoverImage());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setTotalDays(plan.getTotalDays());
        vo.setIsPublic(plan.getIsPublic() == 1);
        vo.setCreatedAt(plan.getCreatedAt());
        return vo;
    }
}
