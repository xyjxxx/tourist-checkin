package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.MerchantPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantPositionMapper extends BaseMapper<MerchantPosition> {

    @Select("SELECT *, " +
            "(6371 * acos(cos(radians(#{lat})) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(#{lng})) + sin(radians(#{lat})) * " +
            "sin(radians(latitude)))) AS distance " +
            "FROM merchant_position WHERE status = 1 " +
            "HAVING distance < #{radius} ORDER BY distance LIMIT #{limit}")
    List<MerchantPosition> selectNearby(@Param("lat") double lat, @Param("lng") double lng,
                                        @Param("radius") double radius, @Param("limit") int limit);
}
