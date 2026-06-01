package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TravelNoteImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TravelNoteImageMapper extends BaseMapper<TravelNoteImage> {

    @Select("SELECT * FROM travel_note_image WHERE note_id = #{noteId} ORDER BY sort_order")
    List<TravelNoteImage> selectByNoteId(@Param("noteId") Long noteId);
}
