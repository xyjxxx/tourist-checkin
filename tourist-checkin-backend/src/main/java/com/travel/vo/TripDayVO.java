package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripDayVO {
    private Long id;
    private Integer dayNumber;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private List<TripPOIVO> pois;
}
