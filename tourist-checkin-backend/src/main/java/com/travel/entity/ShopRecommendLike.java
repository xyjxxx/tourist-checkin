package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("shop_recommend_like")
public class ShopRecommendLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recommendId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
