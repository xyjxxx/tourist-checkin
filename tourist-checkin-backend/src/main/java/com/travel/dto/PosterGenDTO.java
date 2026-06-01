package com.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PosterGenDTO {
    @NotNull(message = "打卡ID不能为空")
    private Long checkInId;

    private String template = "default";

    private Integer width = 750;

    private Integer height = 1334;
}
