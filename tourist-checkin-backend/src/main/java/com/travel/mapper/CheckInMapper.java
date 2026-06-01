package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.CheckIn;
import com.travel.utils.JsonListTypeHandler;
import com.travel.vo.CheckInVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    @Select("SELECT c.id, c.user_id, c.location_id, c.longitude, c.latitude, " +
            "c.location_name, c.content, c.images, c.check_in_time, " +
            "u.username, u.avatar, " +
            "COUNT(l.id) as likeCount, " +
            "SUM(CASE WHEN l.user_id = #{currentUserId} THEN 1 ELSE 0 END) > 0 as hasLiked " +
            "FROM check_in c " +
            "JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN check_in_like l ON l.check_in_id = c.id " +
            "WHERE c.user_id = #{userId} AND c.deleted = 0 " +
            "GROUP BY c.id, u.username, u.avatar " +
            "ORDER BY c.check_in_time DESC")
    @Results(id = "checkInVOMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "locationId", column = "location_id"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "locationName", column = "location_name"),
            @Result(property = "content", column = "content"),
            @Result(property = "images", column = "images", javaType = List.class, jdbcType = JdbcType.VARCHAR, typeHandler = JsonListTypeHandler.class),
            @Result(property = "checkInTime", column = "check_in_time"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "likeCount", column = "likeCount"),
            @Result(property = "hasLiked", column = "hasLiked")
    })
    List<CheckInVO> selectByUserId(@Param("userId") Long userId, @Param("currentUserId") Long currentUserId);

    @Select("SELECT c.id, c.user_id, c.location_id, c.longitude, c.latitude, " +
            "c.location_name, c.content, c.images, c.check_in_time, " +
            "u.username, u.avatar, " +
            "COUNT(l.id) as likeCount, " +
            "SUM(CASE WHEN l.user_id = #{userId} THEN 1 ELSE 0 END) > 0 as hasLiked " +
            "FROM check_in c " +
            "JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN check_in_like l ON l.check_in_id = c.id " +
            "WHERE c.deleted = 0 " +
            "GROUP BY c.id, u.username, u.avatar " +
            "ORDER BY c.check_in_time DESC " +
            "LIMIT 50")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "locationId", column = "location_id"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "locationName", column = "location_name"),
            @Result(property = "content", column = "content"),
            @Result(property = "images", column = "images", javaType = List.class, jdbcType = JdbcType.VARCHAR, typeHandler = JsonListTypeHandler.class),
            @Result(property = "checkInTime", column = "check_in_time"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "likeCount", column = "likeCount"),
            @Result(property = "hasLiked", column = "hasLiked")
    })
    List<CheckInVO> selectRecent(@Param("userId") Long userId);

    @Select("SELECT c.id, c.user_id, c.location_id, c.longitude, c.latitude, " +
            "c.location_name, c.content, c.images, c.check_in_time, " +
            "u.username, u.avatar, " +
            "COUNT(l.id) as likeCount, " +
            "SUM(CASE WHEN l.user_id = #{currentUserId} THEN 1 ELSE 0 END) > 0 as hasLiked " +
            "FROM check_in c " +
            "JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN check_in_like l ON l.check_in_id = c.id " +
            "WHERE c.deleted = 0 " +
            "GROUP BY c.id, u.username, u.avatar " +
            "ORDER BY c.check_in_time DESC")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "locationId", column = "location_id"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "locationName", column = "location_name"),
            @Result(property = "content", column = "content"),
            @Result(property = "images", column = "images", javaType = List.class, jdbcType = JdbcType.VARCHAR, typeHandler = JsonListTypeHandler.class),
            @Result(property = "checkInTime", column = "check_in_time"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "likeCount", column = "likeCount"),
            @Result(property = "hasLiked", column = "hasLiked")
    })
    List<CheckInVO> selectAllCheckIns(@Param("currentUserId") Long currentUserId);

    @Select("SELECT COUNT(*) FROM check_in WHERE deleted = 0 AND DATE(check_in_time) = CURDATE()")
    Long selectTodayCount();

    @Select("SELECT c.id, c.user_id, c.location_id, c.longitude, c.latitude, " +
            "c.location_name, c.content, c.images, c.check_in_time, " +
            "u.username, u.avatar, " +
            "COUNT(l2.id) as likeCount, " +
            "1 as hasLiked " +
            "FROM check_in_like l " +
            "JOIN check_in c ON l.check_in_id = c.id " +
            "JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN check_in_like l2 ON l2.check_in_id = c.id " +
            "WHERE l.user_id = #{userId} AND c.deleted = 0 " +
            "GROUP BY c.id, u.username, u.avatar " +
            "ORDER BY l.created_at DESC")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "locationId", column = "location_id"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "locationName", column = "location_name"),
            @Result(property = "content", column = "content"),
            @Result(property = "images", column = "images", javaType = List.class, jdbcType = JdbcType.VARCHAR, typeHandler = JsonListTypeHandler.class),
            @Result(property = "checkInTime", column = "check_in_time"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "likeCount", column = "likeCount"),
            @Result(property = "hasLiked", column = "hasLiked")
    })
    List<CheckInVO> selectLikedByUserId(@Param("userId") Long userId);
}
