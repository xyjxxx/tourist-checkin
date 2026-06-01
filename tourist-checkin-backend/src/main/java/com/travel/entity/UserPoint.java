package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_point")
public class UserPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer totalPoints;
    private Integer currentPoints;
    private Integer level;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
