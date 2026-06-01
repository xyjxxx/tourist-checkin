package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("check_in_topic")
public class CheckInTopic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long checkInId;
    private Long topicId;
    private LocalDateTime createdAt;
}
