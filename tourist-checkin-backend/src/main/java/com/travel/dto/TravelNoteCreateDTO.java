package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TravelNoteCreateDTO {
    @NotBlank(message = "游记标题不能为空")
    @Size(max = 100, message = "游记标题不能超过100字")
    private String title;

    @Size(max = 500, message = "游记摘要不能超过500字")
    private String summary;

    private String coverImage;

    @NotBlank(message = "游记内容不能为空")
    private String content;

    private String city;

    private List<String> tags;

    private List<Long> checkInPointIds;

    private List<String> images;
}
