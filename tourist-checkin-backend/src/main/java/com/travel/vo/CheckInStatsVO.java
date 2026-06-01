package com.travel.vo;

import lombok.Data;

@Data
public class CheckInStatsVO {
    private Long totalCheckIns;
    private Long todayCheckIns;
    private Long totalLikes;
    private Long totalUsers;
    private Long totalLocations;
}
