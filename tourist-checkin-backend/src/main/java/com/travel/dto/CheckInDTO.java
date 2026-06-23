package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "地点名称不能超过100字")
    private String locationName;

    @Size(max = 500, message = "打卡内容不能超过500字")
    private String content;

    private List<String> images;

    private List<Long> topicIds;
}
