package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TravelNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TravelNoteMapper extends BaseMapper<TravelNote> {

    @Select("SELECT * FROM travel_note WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<TravelNote> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM travel_note WHERE status = 1 ORDER BY view_count DESC, created_at DESC LIMIT #{limit}")
    List<TravelNote> selectHot(@Param("limit") int limit);

    @Select("SELECT * FROM travel_note WHERE status = 1 ORDER BY created_at DESC LIMIT 50")
    List<TravelNote> selectRecent();

    @Select("SELECT tn.* FROM travel_note_like tnl " +
            "JOIN travel_note tn ON tnl.note_id = tn.id " +
            "WHERE tnl.user_id = #{userId} AND tn.status = 1 " +
            "ORDER BY tnl.created_at DESC")
    List<TravelNote> selectLikedByUserId(@Param("userId") Long userId);

    @Update("UPDATE travel_note SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE travel_note SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE travel_note SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    int decrementLikeCount(@Param("id") Long id);

    @Update("UPDATE travel_note SET collect_count = collect_count + 1 WHERE id = #{id}")
    int incrementCollectCount(@Param("id") Long id);

    @Update("UPDATE travel_note SET collect_count = GREATEST(collect_count - 1, 0) WHERE id = #{id}")
    int decrementCollectCount(@Param("id") Long id);

    @Select("SELECT tn.* FROM travel_note_collect tnc " +
            "JOIN travel_note tn ON tnc.note_id = tn.id " +
            "WHERE tnc.user_id = #{userId} AND tn.status = 1 " +
            "ORDER BY tnc.created_at DESC")
    List<TravelNote> selectCollectedByUserId(@Param("userId") Long userId);
}
