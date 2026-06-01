package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.CheckInLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CheckInLikeMapper extends BaseMapper<CheckInLike> {

    @Select("SELECT COUNT(*) FROM check_in_like WHERE check_in_id = #{checkInId}")
    Integer countByCheckInId(@Param("checkInId") Long checkInId);

    @Select("SELECT COUNT(*) > 0 FROM check_in_like WHERE check_in_id = #{checkInId} AND user_id = #{userId}")
    Boolean hasLiked(@Param("checkInId") Long checkInId, @Param("userId") Long userId);
}
