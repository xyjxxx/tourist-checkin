package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.Follow;
import com.travel.exception.BadRequestException;
import com.travel.entity.User;
import com.travel.mapper.FollowMapper;
import com.travel.mapper.UserMapper;
import com.travel.utils.NotificationUtil;
import com.travel.utils.PointUtil;
import com.travel.vo.UserBriefVO;
import com.travel.vo.FollowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final NotificationUtil notificationUtil;
    private final PointUtil pointUtil;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BadRequestException("不能关注自己");
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
               .eq(Follow::getFolloweeId, followeeId);

        Follow follow = followMapper.selectOne(wrapper);
        if (follow == null) {
            try {
                follow = new Follow();
                follow.setFollowerId(followerId);
                follow.setFolloweeId(followeeId);
                follow.setStatus(1);
                followMapper.insert(follow);
                notificationUtil.createAndPush(followeeId, followerId,
                        "FOLLOW", "USER", followerId, "关注了你");
                pointUtil.addPoints(followerId, "FOLLOW", PointUtil.POINTS_FOLLOW,
                        "关注用户", "FOLLOW", followeeId);
            } catch (DuplicateKeyException e) {
                // 并发关注导致唯一索引冲突，忽略（幂等处理）
            }
        } else if (follow.getStatus() == 0) {
            follow.setStatus(1);
            followMapper.updateById(follow);
        }
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
               .eq(Follow::getFolloweeId, followeeId);
        Follow follow = followMapper.selectOne(wrapper);
        if (follow != null) {
            follow.setStatus(0);
            followMapper.updateById(follow);
        }
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        return followMapper.isFollowing(followerId, followeeId);
    }

    public List<FollowVO> getFollowers(Long userId) {
        List<Follow> follows = followMapper.selectFollowers(userId);
        Map<Long, User> userMap = batchGetUsers(follows.stream().map(Follow::getFollowerId).toList());
        return follows.stream()
                .map(f -> {
                    FollowVO vo = new FollowVO();
                    UserBriefVO userVO = toBriefVO(f.getFollowerId(), userMap);
                    vo.setUser(userVO);
                    return vo;
                }).toList();
    }

    public List<FollowVO> getFollowing(Long userId) {
        List<Follow> follows = followMapper.selectFollowing(userId);
        Map<Long, User> userMap = batchGetUsers(follows.stream().map(Follow::getFolloweeId).toList());
        return follows.stream()
                .map(f -> {
                    FollowVO vo = new FollowVO();
                    UserBriefVO userVO = toBriefVO(f.getFolloweeId(), userMap);
                    vo.setUser(userVO);
                    return vo;
                }).toList();
    }

    private Map<Long, User> batchGetUsers(List<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private UserBriefVO toBriefVO(Long userId, Map<Long, User> userMap) {
        UserBriefVO vo = new UserBriefVO();
        vo.setId(userId);
        User user = userMap.get(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }
}
