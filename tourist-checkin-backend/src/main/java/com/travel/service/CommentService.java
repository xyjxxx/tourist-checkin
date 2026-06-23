package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.CommentCreateDTO;
import com.travel.entity.Comment;
import com.travel.entity.CommentLike;
import com.travel.exception.BadRequestException;
import com.travel.exception.UnauthorizedException;
import com.travel.entity.CheckIn;
import com.travel.mapper.CheckInMapper;
import com.travel.mapper.CommentLikeMapper;
import com.travel.mapper.CommentMapper;
import com.travel.utils.NotificationUtil;
import com.travel.utils.PointUtil;
import com.travel.utils.SensitiveWordUtil;
import com.travel.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final CheckInMapper checkInMapper;
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
        comment.setNoteId(dto.getNoteId());
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
        } else if (dto.getCheckInId() != null) {
            CheckIn checkIn = checkInMapper.selectById(dto.getCheckInId());
            if (checkIn != null && !checkIn.getUserId().equals(userId)) {
                notificationUtil.createAndPush(checkIn.getUserId(), userId,
                        "COMMENT", "CHECK_IN", dto.getCheckInId(), "评论了你的打卡");
            }
        }

        // 积分
        pointUtil.addPoints(userId, "COMMENT", PointUtil.POINTS_COMMENT,
                "发表评论", "COMMENT", comment.getId());

        return convertToVO(comment, userId);
    }

    public List<CommentVO> pageByCheckIn(Long checkInId, Long currentUserId, int page, int size) {
        if (checkInId == null) return List.of();
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        List<CommentVO> topComments = commentMapper.selectTopLevelByCheckInId(checkInId, offset, size);
        for (CommentVO top : topComments) {
            List<CommentVO> replies = commentMapper.selectRepliesByParentId(top.getId());
            top.setReplies(replies);
        }
        return topComments;
    }

    public List<CommentVO> pageByNote(Long noteId, Long currentUserId, int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        List<CommentVO> topComments = commentMapper.selectTopLevelByNoteId(noteId, offset, size);
        for (CommentVO top : topComments) {
            List<CommentVO> replies = commentMapper.selectRepliesByParentId(top.getId());
            top.setReplies(replies);
        }
        return topComments;
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

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminList(int page, int size) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Comment::getStatus, 0)
               .orderByDesc(Comment::getCreatedAt);
        Page<Comment> p = new Page<>(page, size);
        Page<Comment> result = commentMapper.selectPage(p, wrapper);

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("list", result.getRecords().stream().map(c -> convertToVO(c, null)).toList());
        map.put("total", result.getTotal());
        return map;
    }

    @Transactional
    public void adminDelete(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BadRequestException("评论不存在");
        comment.setStatus(0);
        commentMapper.updateById(comment);
    }

    private CommentVO convertToVO(Comment c, Long currentUserId) {
        CommentVO vo = commentMapper.selectVOById(c.getId());
        if (vo != null) return vo;
        // fallback
        vo = new CommentVO();
        vo.setId(c.getId());
        vo.setCheckInId(c.getCheckInId());
        vo.setNoteId(c.getNoteId());
        vo.setContent(c.getContent());
        vo.setLikeCount(c.getLikeCount());
        vo.setStatus(c.getStatus());
        vo.setParentId(c.getParentId());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }
}
