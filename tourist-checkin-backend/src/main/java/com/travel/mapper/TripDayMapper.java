package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TripDay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TripDayMapper extends BaseMapper<TripDay> {

    @Select("SELECT * FROM trip_day WHERE plan_id = #{planId} ORDER BY day_number")
    List<TripDay> selectByPlanId(@Param("planId") Long planId);
}
