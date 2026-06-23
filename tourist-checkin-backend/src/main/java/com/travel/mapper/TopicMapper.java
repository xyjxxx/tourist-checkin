package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    @Select("SELECT * FROM topic WHERE is_hot = 1 OR check_in_count > 0 " +
            "ORDER BY check_in_count DESC, view_count DESC LIMIT 20")
    List<Topic> selectTrending();

    @Select("SELECT * FROM topic WHERE name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY check_in_count DESC LIMIT 20")
    List<Topic> searchByKeyword(@Param("keyword") String keyword);

    @Update("UPDATE topic SET check_in_count = check_in_count + 1 WHERE id = #{id}")
    int incrementCheckInCount(@Param("id") Long id);
}
