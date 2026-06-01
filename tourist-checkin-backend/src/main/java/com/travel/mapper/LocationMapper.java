package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Location;
import com.travel.vo.LocationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LocationMapper extends BaseMapper<Location> {

    @Select("SELECT * FROM location " +
            "WHERE longitude BETWEEN #{minLng} AND #{maxLng} " +
            "AND latitude BETWEEN #{minLat} AND #{maxLat} " +
            "LIMIT 50")
    List<LocationVO> selectNearby(@Param("minLng") BigDecimal minLng,
                                   @Param("maxLng") BigDecimal maxLng,
                                   @Param("minLat") BigDecimal minLat,
                                   @Param("maxLat") BigDecimal maxLat);

    @Select("SELECT * FROM location WHERE city = #{city} LIMIT 100")
    List<LocationVO> selectByCity(@Param("city") String city);
}
