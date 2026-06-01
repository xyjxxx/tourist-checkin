package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.CommentCreateDTO;
import com.travel.entity.Comment;
import com.travel.entity.CommentLike;
import com.travel.exception.BadRequestException;
import com.travel.exception.UnauthorizedException;
import com.travel.mapper.CommentLikeMapper;
import com.travel.mapper.CommentMapper;
import com.travel.utils.NotificationUtil;
import com.travel.utils.PointUtil;
import com.travel.utils.SensitiveWordUtil;
import com.travel.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final SensitiveWordUtil sensitiveWordUtil;
    private final NotificationUtil notificationUtil;
    private final PointUtil pointUtil;

    @Transactional
    public CommentVO create(Long userId, CommentCreateDTO dto) {
        // 敏感词过滤
        if (sensitiveWordUtil.containsSensitive(dto.getContent())) {
            dto.setContent(sensitiveWordUtil.replace(dto.getContent()));
        }

        Comment comment = new Comment();
        comment.setCheckInId(dto.getCheckInId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyToId(dto.getReplyToId());
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        // 发送通知
        if (dto.getReplyToUserId() != null) {
            notificationUtil.createAndPush(dto.getReplyToUserId(), userId,
                    "REPLY", "COMMENT", comment.getId(), "回复了你的评论");
        } else {
            // 通知打卡作者（通过关联查询获取）
            notificationUtil.createAndPush(dto.getCheckInId(), userId,
                    "COMMENT", "CHECK_IN", dto.getCheckInId(), "评论了你的打卡");
        }

        // 积分
        pointUtil.addPoints(userId, "COMMENT", PointUtil.POINTS_COMMENT,
                "发表评论", "COMMENT", comment.getId());

        return convertToVO(comment, userId);
    }

    public List<CommentVO> pageByCheckIn(Long checkInId, Long currentUserId, int page, int size) {
        List<CommentVO> all = commentMapper.selectByCheckInId(checkInId, currentUserId);
        // 构建树形结构：顶级评论 + 嵌套回复
        Map<Long, List<CommentVO>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        return all.stream()
                .filter(c -> c.getParentId() == null)
                .peek(c -> c.setReplies(childrenMap.getOrDefault(c.getId(), List.of())))
                .skip((long) (page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Transactional
    public void like(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getUserId, userId);

        if (commentLikeMapper.selectCount(wrapper) > 0) {
            // 已点赞，执行取消点赞（原子递减）
            commentLikeMapper.delete(wrapper);
            commentMapper.decrementLikeCount(commentId);
        } else {
            // 未点赞，执行点赞（捕获并发唯一索引冲突，实现幂等）
            try {
                CommentLike like = new CommentLike();
                like.setCommentId(commentId);
                like.setUserId(userId);
                commentLikeMapper.insert(like);
                commentMapper.incrementLikeCount(commentId);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发场景下唯一索引冲突：其他线程已点赞，幂等处理
            }
        }
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权删除");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
    }

    private CommentVO convertToVO(Comment c, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setCheckInId(c.getCheckInId());
        vo.setContent(c.getContent());
        vo.setLikeCount(c.getLikeCount());
        vo.setStatus(c.getStatus());
        vo.setParentId(c.getParentId());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }
}
