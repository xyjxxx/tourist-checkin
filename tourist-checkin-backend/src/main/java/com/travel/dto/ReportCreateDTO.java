package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCreateDTO {
    @NotBlank(message = "举报目标类型不能为空")
    private String targetType;

    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;

    @NotNull(message = "被举报用户ID不能为空")
    private Long reportedUserId;

    @NotBlank(message = "举报原因不能为空")
    private String reason;

    private String detail;
}
