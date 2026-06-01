package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageAuditVO {
    private Long id;
    private String imageUrl;
    private UserBriefVO uploader;
    private String sourceType;
    private Long sourceId;
    private Integer auditStatus;
    private String auditResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
