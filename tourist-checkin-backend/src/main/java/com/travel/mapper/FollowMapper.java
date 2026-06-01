package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    @Select("SELECT COUNT(*) > 0 FROM follow " +
            "WHERE follower_id = #{followerId} AND followee_id = #{followeeId} AND status = 1")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    @Select("SELECT * FROM follow WHERE followee_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<Follow> selectFollowers(@Param("userId") Long userId);

    @Select("SELECT * FROM follow WHERE follower_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<Follow> selectFollowing(@Param("userId") Long userId);
}
