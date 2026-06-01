package com.travel.vo;

import lombok.Data;

@Data
public class UserReportVO {
    private Long totalCheckIns;
    private Long totalCities;
    private Long totalLikes;
    private Long totalDays;
    private Integer longestStreak;
    private String mostActiveMonth;
    private String mostActiveCity;
    private String favoriteCategory;
    private Long userId;
    private String username;
}
