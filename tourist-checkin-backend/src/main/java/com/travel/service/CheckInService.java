package com.travel.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.travel.dto.CheckInDTO;
import com.travel.entity.*;
import com.travel.exception.UnauthorizedException;
import com.travel.mapper.*;
import com.travel.utils.AchievementUtil;
import com.travel.utils.NotificationUtil;
import com.travel.utils.PointUtil;
import com.travel.vo.CheckInStatsVO;
import com.travel.vo.CheckInVO;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInMapper checkInMapper;
    private final CheckInLikeMapper checkInLikeMapper;
    private final CommentMapper commentMapper;
    private final CheckInTopicMapper checkInTopicMapper;
    private final com.travel.mapper.UserMapper userMapper;
    private final TopicService topicService;
    private final PointUtil pointUtil;
    private final AchievementUtil achievementUtil;
    private final NotificationUtil notificationUtil;

    @Transactional
    public Long createCheckIn(Long userId, CheckInDTO dto) {
        CheckIn checkIn = new CheckIn();
        BeanUtils.copyProperties(dto, checkIn);
        checkIn.setUserId(userId);
        // 兜底：确保打卡时间不为空（兼容自动填充未生效场景）
        if (checkIn.getCheckInTime() == null) {
            checkIn.setCheckInTime(LocalDateTime.now());
        }

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            checkIn.setImages(JSON.toJSONString(dto.getImages()));
        }

        checkInMapper.insert(checkIn);

        // 关联话题
        if (dto.getTopicIds() != null && !dto.getTopicIds().isEmpty()) {
            topicService.attachToCheckIn(userId, checkIn.getId(), dto.getTopicIds());
        }

        // 奖励积分
        pointUtil.addPoints(userId, "CHECK_IN", PointUtil.POINTS_CHECK_IN,
                "完成打卡", "CHECK_IN", checkIn.getId());

        // 检查成就
        long checkInCount = checkInMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId));
        achievementUtil.checkAndUnlock(userId, "CHECK_IN_COUNT", (int) checkInCount);
        achievementUtil.checkAndUnlock(userId, "TOTAL_CHECK_INS", (int) checkInCount);

        return checkIn.getId();
    }

    public List<CheckInVO> listByUserId(Long userId, Long currentUserId) {
        return checkInMapper.selectByUserId(userId, currentUserId);
    }

    public CheckInVO getDetailById(Long id, Long currentUserId) {
        return checkInMapper.selectDetailById(id, currentUserId);
    }

    public List<CheckInVO> listRecent(Long userId) {
        return checkInMapper.selectRecent(userId);
    }

    public List<CheckInVO> listFollowing(Long userId, int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        return checkInMapper.selectFollowing(userId, offset, size);
    }

    public List<CheckInVO> listHotThisMonth(Long userId, int limit) {
        return checkInMapper.selectHotThisMonth(userId, limit);
    }

    @Transactional
    public void likeCheckIn(Long userId, Long checkInId) {
        LambdaQueryWrapper<CheckInLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInLike::getCheckInId, checkInId)
               .eq(CheckInLike::getUserId, userId);

        if (checkInLikeMapper.selectCount(wrapper) > 0) {
            // 已点赞，执行取消点赞
            checkInLikeMapper.delete(wrapper);
            checkInMapper.update(null, new LambdaUpdateWrapper<CheckIn>()
                    .eq(CheckIn::getId, checkInId)
                    .setSql("like_count = like_count - 1"));
        } else {
            // 未点赞，执行点赞（捕获并发唯一索引冲突，实现幂等）
            try {
                CheckInLike like = new CheckInLike();
                like.setCheckInId(checkInId);
                like.setUserId(userId);
                checkInLikeMapper.insert(like);
                checkInMapper.update(null, new LambdaUpdateWrapper<CheckIn>()
                        .eq(CheckIn::getId, checkInId)
                        .setSql("like_count = like_count + 1"));

                // 点赞通知
                CheckIn checkIn = checkInMapper.selectById(checkInId);
                if (checkIn != null && !checkIn.getUserId().equals(userId)) {
                    notificationUtil.createAndPush(checkIn.getUserId(), userId,
                            "LIKE", "CHECK_IN", checkInId, "点赞了你的打卡");
                }

                // 积分
                pointUtil.addPoints(userId, "LIKE", PointUtil.POINTS_LIKE,
                        "点赞打卡", "CHECK_IN", checkInId);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发场景下唯一索引冲突：其他线程已点赞，幂等处理
                // 无需抛异常，视为点赞成功（或已存在）
            }
        }
    }

    @Transactional
    public void deleteCheckIn(Long userId, Long checkInId) {
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null || !checkIn.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权删除此打卡记录");
        }
        cascadeDeleteCheckIn(checkInId);
    }

    public List<CheckInVO> getAllCheckIns(Long currentUserId) {
        return checkInMapper.selectAllCheckIns(currentUserId, 200);
    }

    public java.util.Map<String, Object> getCheckInPage(String keyword, int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;
        List<CheckInVO> list = checkInMapper.selectPage(keyword, offset, size);
        long total = checkInMapper.selectPageCount(keyword);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    public List<CheckInVO> getLikedCheckIns(Long userId) {
        return checkInMapper.selectLikedByUserId(userId);
    }

    public CheckInStatsVO getCheckInStats() {
        CheckInStatsVO stats = new CheckInStatsVO();
        stats.setTotalCheckIns(checkInMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckIn>().eq(CheckIn::getDeleted, 0)));
        stats.setTodayCheckIns(checkInMapper.selectTodayCount());
        stats.setTotalLikes(checkInLikeMapper.selectCount(null));
        stats.setTotalUsers(checkInMapper.selectDistinctUserCount());
        stats.setTotalLocations(checkInMapper.selectDistinctLocationCount());
        return stats;
    }

    @Transactional
    public void adminDeleteCheckIn(Long adminUserId, Long checkInId) {
        User admin = userMapper.selectById(adminUserId);
        if (admin == null || (!"ADMIN".equals(admin.getRole()) && !"SUPER_ADMIN".equals(admin.getRole()))) {
            throw new UnauthorizedException("无权访问");
        }
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null) {
            throw new com.travel.exception.BadRequestException("打卡记录不存在");
        }
        cascadeDeleteCheckIn(checkInId);
    }

    private void cascadeDeleteCheckIn(Long checkInId) {
        // 级联清理关联数据
        checkInLikeMapper.delete(new LambdaQueryWrapper<CheckInLike>().eq(CheckInLike::getCheckInId, checkInId));
        // 评论使用软删除（status=0），与其他删除路径保持一致
        Comment commentUpdate = new Comment();
        commentUpdate.setStatus(0);
        commentMapper.update(commentUpdate, new LambdaQueryWrapper<Comment>().eq(Comment::getCheckInId, checkInId));
        checkInTopicMapper.delete(new LambdaQueryWrapper<CheckInTopic>().eq(CheckInTopic::getCheckInId, checkInId));
        checkInMapper.deleteById(checkInId);
    }
}
