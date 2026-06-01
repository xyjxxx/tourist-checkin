package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sensitive_word")
public class SensitiveWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String category;
    private Integer level;
    private Integer isEnabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
