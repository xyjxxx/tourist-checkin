package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Comment;
import com.travel.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.id, c.check_in_id, c.user_id, c.parent_id, c.reply_to_id, " +
            "c.reply_to_user_id, c.content, c.like_count, c.status, c.created_at, " +
            "u.username, u.avatar " +
            "FROM comment c JOIN user u ON c.user_id = u.id " +
            "WHERE c.check_in_id = #{checkInId} AND c.status = 1 " +
            "ORDER BY c.created_at ASC")
    List<CommentVO> selectByCheckInId(@Param("checkInId") Long checkInId,
                                      @Param("currentUserId") Long currentUserId);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(@Param("id") Long id);
}
