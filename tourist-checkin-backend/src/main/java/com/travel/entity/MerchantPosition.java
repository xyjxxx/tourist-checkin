package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_position")
public class MerchantPosition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String city;
    private BigDecimal rating;
    private Integer priceLevel;
    private String tags;
    private String coverImage;
    private String phone;
    private String businessHours;
    private String description;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
