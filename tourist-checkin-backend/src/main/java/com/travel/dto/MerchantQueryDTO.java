package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantQueryDTO {
    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer radius = 5000;

    private String category;

    private Integer priceLevel;

    private Integer page = 1;

    private Integer size = 10;
}
