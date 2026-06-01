package com.travel.service;

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
        plan.setIsPublic(dto.getIsPublic() != null ? 1 : 0);
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
        return tripPlanMapper.selectPublic(size).stream()
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

        List<TripDay> days = tripDayMapper.selectByPlanId(plan.getId());
        vo.setDays(days.stream().map(day -> {
            TripDayVO dayVO = new TripDayVO();
            dayVO.setId(day.getId());
            dayVO.setDayNumber(day.getDayNumber());
            dayVO.setTitle(day.getTitle());
            dayVO.setDate(day.getDate());

            List<TripPOI> pois = tripPOIMapper.selectByDayId(day.getId());
            dayVO.setPois(pois.stream().map(poi -> {
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
