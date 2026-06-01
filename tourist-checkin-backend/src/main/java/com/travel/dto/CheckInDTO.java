package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckInDTO {
    private Long locationId;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @NotBlank(message = "地点名称不能为空")
    private String locationName;

    private String content;

    private List<String> images;

    private List<Long> topicIds;
}
