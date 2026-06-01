package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String category;
    private Integer level;
    private String conditionType;
    private Integer conditionValue;
    private Integer pointsReward;
    private Integer isHidden;
    private LocalDateTime createdAt;
}
