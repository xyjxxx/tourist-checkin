package com.travel.vo;

import lombok.Data;

@Data
public class UserPointVO {
    private Integer totalPoints;
    private Integer currentPoints;
    private Integer level;
    private String levelName;
    private Integer nextLevelPoints;
}
