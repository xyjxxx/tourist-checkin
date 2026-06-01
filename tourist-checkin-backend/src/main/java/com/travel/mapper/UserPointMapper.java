package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.UserPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserPointMapper extends BaseMapper<UserPoint> {

    /**
     * 原子增加积分，避免并发丢失更新
     */
    @Update("UPDATE user_point SET total_points = total_points + #{points}, current_points = current_points + #{points} WHERE user_id = #{userId}")
    int addPointsAtomic(@Param("userId") Long userId, @Param("points") int points);
}
