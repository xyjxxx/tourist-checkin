package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("location")
public class Location {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String category;

    private String city;

    private String description;

    private String coverImage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
