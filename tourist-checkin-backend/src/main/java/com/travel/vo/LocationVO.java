package com.travel.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LocationVO {
    private Long id;
    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String category;
    private String city;
    private String description;
    private String coverImage;
    private LocalDateTime createdAt;
}
