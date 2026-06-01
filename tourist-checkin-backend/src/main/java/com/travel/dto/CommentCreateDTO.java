package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDTO {
    @NotNull(message = "打卡ID不能为空")
    private Long checkInId;

    private Long parentId;

    private Long replyToId;

    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;
}
