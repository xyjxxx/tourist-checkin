package com.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripDayDTO {
    @NotNull(message = "天数序号不能为空")
    private Integer dayNumber;

    private String title;

    private LocalDate date;

    private List<TripPOIDTO> pois;
}
