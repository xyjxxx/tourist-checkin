package com.travel.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChartVO {
    private List<String> labels;
    private List<Integer> values;
    private String seriesName;
}
