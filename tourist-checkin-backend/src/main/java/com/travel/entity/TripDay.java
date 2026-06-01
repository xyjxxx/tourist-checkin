package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("trip_day")
public class TripDay {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Integer dayNumber;
    private String title;
    private LocalDate date;
    private Integer sortOrder;
}
