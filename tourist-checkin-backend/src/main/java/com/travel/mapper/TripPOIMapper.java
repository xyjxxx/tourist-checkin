package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TripPOI;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TripPOIMapper extends BaseMapper<TripPOI> {

    @Select("SELECT * FROM trip_poi WHERE day_id = #{dayId} ORDER BY sort_order")
    List<TripPOI> selectByDayId(@Param("dayId") Long dayId);

    default List<TripPOI> selectByDayIds(List<Long> dayIds) {
        return selectList(new LambdaQueryWrapper<TripPOI>()
                .in(TripPOI::getDayId, dayIds)
                .orderByAsc(TripPOI::getSortOrder));
    }
}
