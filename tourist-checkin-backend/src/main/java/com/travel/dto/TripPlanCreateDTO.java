package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripPlanCreateDTO {
    @NotBlank(message = "行程标题不能为空")
    @Size(max = 200, message = "行程标题不能超过200字")
    private String title;

    @Size(max = 1000, message = "行程描述不能超过1000字")
    private String description;

    private String city;

    private String coverImage;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isPublic;

    private List<TripDayDTO> days;
}
