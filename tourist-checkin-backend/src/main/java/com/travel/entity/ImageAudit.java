package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("image_audit")
public class ImageAudit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String imageUrl;
    private Long uploaderId;
    private String sourceType;
    private Long sourceId;
    private Integer auditStatus;
    private String auditResult;
    private Long auditorId;
    private LocalDateTime auditTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
