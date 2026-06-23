package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.ShopRecommend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

@Mapper
public interface ShopRecommendMapper extends BaseMapper<ShopRecommend> {

    @Select("SELECT sr.*, " +
            "6371 * ACOS(COS(RADIANS(#{lat})) * COS(RADIANS(sr.latitude)) * COS(RADIANS(sr.longitude) - RADIANS(#{lng})) + SIN(RADIANS(#{lat})) * SIN(RADIANS(sr.latitude))) AS distance " +
            "FROM shop_recommend sr " +
            "WHERE sr.audit_status = 1 AND sr.deleted = 0 " +
            "HAVING distance < #{radius} " +
            "ORDER BY distance " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") int radius, @Param("limit") int limit);

    @Select("SELECT sr.*, " +
            "6371 * ACOS(COS(RADIANS(#{lat})) * COS(RADIANS(sr.latitude)) * COS(RADIANS(sr.longitude) - RADIANS(#{lng})) + SIN(RADIANS(#{lat})) * SIN(RADIANS(sr.latitude))) AS distance " +
            "FROM shop_recommend sr " +
            "WHERE sr.audit_status = 1 AND sr.deleted = 0 AND sr.category = #{category} " +
            "HAVING distance < #{radius} " +
            "ORDER BY distance " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectNearbyByCategory(@Param("lat") double lat, @Param("lng") double lng,
                                                      @Param("radius") int radius, @Param("category") String category,
                                                      @Param("offset") int offset, @Param("size") int size);

    @Update("UPDATE shop_recommend SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE shop_recommend SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    int decrementLikeCount(@Param("id") Long id);
}
