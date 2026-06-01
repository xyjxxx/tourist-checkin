package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripPlanCreateDTO {
    @NotBlank(message = "行程标题不能为空")
    private String title;

    private String description;

    private String city;

    private String coverImage;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isPublic;

    private List<TripDayDTO> days;
}
