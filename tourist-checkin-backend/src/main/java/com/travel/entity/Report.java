package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private String targetType;
    private Long targetId;
    private String reason;
    private String detail;
    private Integer status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime handleTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
