package com.travel.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TripPOIVO {
    private Long id;
    private String name;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private String category;
    private String notes;
    private Integer durationMinutes;
    private Integer sortOrder;
}
