package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("check_in")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long locationId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String locationName;

    private String content;

    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime checkInTime;

    private Integer likeCount;

    @TableLogic
    private Integer deleted;
}
