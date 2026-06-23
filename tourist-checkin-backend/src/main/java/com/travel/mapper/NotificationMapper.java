package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Notification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    void markAllRead(@Param("userId") Long userId);

    @Insert("<script>" +
            "INSERT INTO notification (user_id, from_user_id, type, target_type, target_id, content, is_read, status, created_at) VALUES " +
            "<foreach collection='list' item='n' separator=','>" +
            "(#{n.userId}, #{n.fromUserId}, #{n.type}, #{n.targetType}, #{n.targetId}, #{n.content}, #{n.isRead}, #{n.status}, NOW())" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("list") List<Notification> notifications);
}
