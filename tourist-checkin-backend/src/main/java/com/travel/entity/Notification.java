package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long fromUserId;
    private String type;
    private String targetType;
    private Long targetId;
    private String content;
    private Integer isRead;
    private Integer status;
    private LocalDateTime createdAt;
}
