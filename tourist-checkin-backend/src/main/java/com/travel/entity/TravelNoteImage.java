package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("travel_note_image")
public class TravelNoteImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private String url;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
