package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TripPOIDTO {
    @NotBlank(message = "POI名称不能为空")
    private String name;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    private String address;

    private String category;

    private String notes;

    private Integer durationMinutes = 60;

    private Integer sortOrder;
}
