package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("check_in_like")
public class CheckInLike {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkInId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
