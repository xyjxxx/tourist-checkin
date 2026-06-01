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
}
