package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TravelNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TravelNoteMapper extends BaseMapper<TravelNote> {

    @Select("SELECT * FROM travel_note WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<TravelNote> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM travel_note WHERE status = 1 ORDER BY view_count DESC, created_at DESC LIMIT #{limit}")
    List<TravelNote> selectHot(@Param("limit") int limit);

    @Select("SELECT * FROM travel_note WHERE status = 1 ORDER BY created_at DESC")
    List<TravelNote> selectRecent();

    @Select("SELECT tn.* FROM travel_note_like tnl " +
            "JOIN travel_note tn ON tnl.note_id = tn.id " +
            "WHERE tnl.user_id = #{userId} AND tn.status = 1 " +
            "ORDER BY tnl.created_at DESC")
    List<TravelNote> selectLikedByUserId(@Param("userId") Long userId);
}
