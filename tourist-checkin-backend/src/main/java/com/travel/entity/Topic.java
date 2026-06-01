package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("topic")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer checkInCount;
    private Integer viewCount;
    private Integer isHot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
