package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "举报原因不能超过50字")
    private String reason;

    @Size(max = 500, message = "举报详情不能超过500字")
    private String detail;
}
