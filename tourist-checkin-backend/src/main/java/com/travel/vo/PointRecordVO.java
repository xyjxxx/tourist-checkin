package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointRecordVO {
    private Long id;
    private String type;
    private Integer points;
    private String description;
    private Integer balanceAfter;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
