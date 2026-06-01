package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trip_poi")
public class TripPOI {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dayId;
    private Long planId;
    private String name;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private String category;
    private String notes;
    private Integer durationMinutes;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
