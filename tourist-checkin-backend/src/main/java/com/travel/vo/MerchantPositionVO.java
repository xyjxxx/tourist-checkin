package com.travel.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantPositionVO {
    private Long id;
    private String name;
    private String category;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Double rating;
    private Integer priceLevel;
    private List<String> tags;
    private String coverImage;
    private String phone;
    private String businessHours;
    private String description;
    private Double distance;
}
