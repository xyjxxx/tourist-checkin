package com.travel.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String account;
    private String username;
    private String avatar;
    private String backgroundImage;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    // 关注状态
    private Boolean isFollowing;

    // 统计字段
    private Integer checkinCount;
    private Integer followerCount;
    private Integer followingCount;
    private Integer points;
    private Integer level;
}
