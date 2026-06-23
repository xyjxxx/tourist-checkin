package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Comment;
import com.travel.vo.CommentVO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.id, c.check_in_id, c.user_id, c.parent_id, c.reply_to_id, " +
            "c.reply_to_user_id, c.content, c.like_count, c.status, c.created_at, " +
            "u.username, u.avatar, ru.username as reply_to_username, ru.avatar as reply_to_avatar " +
            "FROM comment c JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.check_in_id = #{checkInId} AND c.status = 1 " +
            "ORDER BY c.created_at ASC")
    @Results(id = "commentVOMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "checkInId", column = "check_in_id"),
            @Result(property = "noteId", column = "note_id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.avatar", column = "avatar"),
            @Result(property = "replyToUser.id", column = "reply_to_user_id"),
            @Result(property = "replyToUser.username", column = "reply_to_username"),
            @Result(property = "replyToUser.avatar", column = "reply_to_avatar")
    })
    List<CommentVO> selectByCheckInId(@Param("checkInId") Long checkInId,
                                      @Param("currentUserId") Long currentUserId);

    @Select("SELECT c.id, c.check_in_id, c.user_id, c.parent_id, c.reply_to_id, " +
            "c.reply_to_user_id, c.content, c.like_count, c.status, c.created_at, " +
            "u.username, u.avatar " +
            "FROM comment c JOIN user u ON c.user_id = u.id " +
            "WHERE c.check_in_id = #{checkInId} AND c.parent_id IS NULL AND c.status = 1 " +
            "ORDER BY c.created_at DESC LIMIT #{offset}, #{size}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "checkInId", column = "check_in_id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<CommentVO> selectTopLevelByCheckInId(@Param("checkInId") Long checkInId,
                                               @Param("offset") int offset,
                                               @Param("size") int size);

    @Select("SELECT c.id, c.check_in_id, c.note_id, c.user_id, c.parent_id, c.reply_to_id, " +
            "c.reply_to_user_id, c.content, c.like_count, c.status, c.created_at, " +
            "u.username, u.avatar " +
            "FROM comment c JOIN user u ON c.user_id = u.id " +
            "WHERE c.note_id = #{noteId} AND c.parent_id IS NULL AND c.status = 1 " +
            "ORDER BY c.created_at DESC LIMIT #{offset}, #{size}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "checkInId", column = "check_in_id"),
            @Result(property = "noteId", column = "note_id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.avatar", column = "avatar")
    })
    List<CommentVO> selectTopLevelByNoteId(@Param("noteId") Long noteId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    @Select("SELECT c.id, c.check_in_id, c.user_id, c.parent_id, c.reply_to_id, " +
            "c.reply_to_user_id, c.content, c.like_count, c.status, c.created_at, " +
            "u.username, u.avatar, ru.username as reply_to_username, ru.avatar as reply_to_avatar " +
            "FROM comment c JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.parent_id = #{parentId} AND c.status = 1 ORDER BY c.created_at ASC")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "checkInId", column = "check_in_id"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "user.id", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.avatar", column = "avatar"),
            @Result(property = "replyToUser.id", column = "reply_to_user_id"),
            @Result(property = "replyToUser.username", column = "reply_to_username"),
            @Result(property = "replyToUser.avatar", column = "reply_to_avatar")
    })
    List<CommentVO> selectRepliesByParentId(@Param("parentId") Long parentId);

    @Select("SELECT c.id, c.check_in_id, c.note_id, c.user_id, c.parent_id, c.reply_to_id, " +
            "c.reply_to_user_id, c.content, c.like_count, c.status, c.created_at, " +
            "u.username, u.avatar, ru.username as reply_to_username, ru.avatar as reply_to_avatar " +
            "FROM comment c JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.id = #{id} AND c.status = 1")
    @ResultMap("commentVOMap")
    CommentVO selectVOById(@Param("id") Long id);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(@Param("id") Long id);
}
