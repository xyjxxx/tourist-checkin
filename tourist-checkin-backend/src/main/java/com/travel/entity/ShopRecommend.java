package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop_recommend")
public class ShopRecommend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String category;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String city;
    private Integer avgPrice;
    private String images;        // JSON string
    private String recommendReason;
    private String signatureDish;
    private String businessHours;
    private String phone;
    private String warning;
    private Integer auditStatus;  // 0=pending, 1=approved, -1=rejected
    private String auditReason;
    private Long auditorId;
    private LocalDateTime auditTime;
    private Long merchantId;
    private Integer likeCount;
    private Integer collectCount;
    private Integer isFeatured;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
