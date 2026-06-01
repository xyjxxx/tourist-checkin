package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AchievementVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String category;
    private Integer level;
    private Integer progress;
    private Boolean isUnlocked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unlockedAt;

    private Integer pointsReward;
}
