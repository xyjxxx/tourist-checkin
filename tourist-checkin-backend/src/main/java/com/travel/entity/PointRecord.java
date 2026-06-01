package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("point_record")
public class PointRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private Integer points;
    private String description;
    private String refType;
    private Long refId;
    private Integer balanceAfter;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
