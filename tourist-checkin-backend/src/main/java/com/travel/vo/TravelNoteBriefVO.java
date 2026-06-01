package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TravelNoteBriefVO {
    private Long id;
    private UserBriefVO author;
    private String title;
    private String summary;
    private String coverImage;
    private String city;
    private Integer viewCount;
    private Integer likeCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
