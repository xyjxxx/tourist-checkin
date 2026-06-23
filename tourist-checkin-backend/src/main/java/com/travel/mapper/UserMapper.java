package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除被软删除的用户（绕过 @TableLogic），用于释放账号唯一键
     */
    @Delete("DELETE FROM user WHERE account = #{account} AND deleted = 1")
    int physicallyDeleteByAccount(@Param("account") String account);

    /**
     * 查询用户统计（打卡数、粉丝数、关注数、积分）
     */
    @Select("SELECT " +
            "COUNT(DISTINCT ci.id) AS checkinCount, " +
            "COUNT(DISTINCT f1.follower_id) AS followerCount, " +
            "COUNT(DISTINCT f2.followee_id) AS followingCount, " +
            "COALESCE(up.total_points, 0) AS points, " +
            "COALESCE(up.level, 0) AS level " +
            "FROM user u " +
            "LEFT JOIN check_in ci ON ci.user_id = u.id AND ci.deleted = 0 " +
            "LEFT JOIN check_in_like cil ON cil.check_in_id = ci.id " +
            "LEFT JOIN follow f1 ON f1.followee_id = u.id AND f1.status = 1 " +
            "LEFT JOIN follow f2 ON f2.follower_id = u.id AND f2.status = 1 " +
            "LEFT JOIN user_point up ON up.user_id = u.id " +
            "WHERE u.id = #{userId} AND u.deleted = 0 " +
            "GROUP BY u.id, up.total_points, up.level")
    Map<String, Object> selectUserStats(@Param("userId") Long userId);
}
