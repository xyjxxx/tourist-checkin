package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private UserBriefVO reporter;
    private UserBriefVO reportedUser;
    private String targetType;
    private Long targetId;
    private String reason;
    private String detail;
    private Integer status;
    private UserBriefVO handler;
    private String handleResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
