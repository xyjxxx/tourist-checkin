package com.travel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private UserBriefVO user;
    private Long checkInId;
    private Long parentId;
    private UserBriefVO replyToUser;
    private String content;
    private Integer likeCount;
    private Boolean hasLiked;
    private Integer status;
    private List<CommentVO> replies;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
