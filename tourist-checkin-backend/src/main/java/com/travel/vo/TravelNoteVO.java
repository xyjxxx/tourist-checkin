package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TravelNoteVO {
    private Long id;
    private UserBriefVO author;
    private String title;
    private String summary;
    private String coverImage;
    private String content;
    private String city;
    private List<String> tags;
    private List<String> images;
    private List<CheckInVO> checkInPoints;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Boolean hasLiked;
    private Boolean hasCollected;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
