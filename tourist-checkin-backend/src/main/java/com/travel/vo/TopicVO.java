package com.travel.vo;

import lombok.Data;

@Data
public class TopicVO {
    private Long id;
    private String name;
    private String icon;
    private String description;
    private Integer checkInCount;
    private Integer viewCount;
    private Boolean isHot;
}
