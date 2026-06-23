package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TripPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TripPlanMapper extends BaseMapper<TripPlan> {

    @Select("SELECT * FROM trip_plan WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<TripPlan> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM trip_plan WHERE status = 1 AND is_public = 1 ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<TripPlan> selectPublic(@Param("offset") int offset, @Param("limit") int limit);
}
