package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.Follow;
import com.travel.exception.BadRequestException;
import com.travel.mapper.FollowMapper;
import com.travel.utils.NotificationUtil;
import com.travel.utils.PointUtil;
import com.travel.vo.UserBriefVO;
import com.travel.vo.FollowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
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
            follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            follow.setStatus(1);
            followMapper.insert(follow);
        } else if (follow.getStatus() == 0) {
            follow.setStatus(1);
            followMapper.updateById(follow);
        }

        notificationUtil.createAndPush(followeeId, followerId,
                "FOLLOW", "USER", followerId, "关注了你");
        pointUtil.addPoints(followerId, "FOLLOW", PointUtil.POINTS_FOLLOW,
                "关注用户", "FOLLOW", followeeId);
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
        return followMapper.selectFollowers(userId).stream()
                .map(f -> {
                    FollowVO vo = new FollowVO();
                    UserBriefVO user = new UserBriefVO();
                    user.setId(f.getFollowerId());
                    vo.setUser(user);
                    return vo;
                }).toList();
    }

    public List<FollowVO> getFollowing(Long userId) {
        return followMapper.selectFollowing(userId).stream()
                .map(f -> {
                    FollowVO vo = new FollowVO();
                    UserBriefVO user = new UserBriefVO();
                    user.setId(f.getFolloweeId());
                    vo.setUser(user);
                    return vo;
                }).toList();
    }
}
