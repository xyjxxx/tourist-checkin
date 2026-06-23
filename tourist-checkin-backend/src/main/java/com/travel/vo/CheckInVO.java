package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CheckInVO {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private Long locationId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String locationName;
    private String content;
    private List<String> images;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean hasLiked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
}
