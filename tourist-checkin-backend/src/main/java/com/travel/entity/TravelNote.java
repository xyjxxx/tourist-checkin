package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("travel_note")
public class TravelNote {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String summary;
    private String coverImage;
    private String content;
    private String city;
    private String tags;
    private String checkInPointIds;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer isPinned;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
