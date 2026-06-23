package com.travel.utils;

import com.travel.entity.Achievement;
import com.travel.entity.UserAchievement;
import com.travel.mapper.AchievementMapper;
import com.travel.mapper.UserAchievementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementUtil {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final PointUtil pointUtil;

    @Transactional
    public void checkAndUnlock(Long userId, String conditionType, int newValue) {
        var achievements = achievementMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getConditionType, conditionType)
        );

        for (Achievement achievement : achievements) {
            var userAchievement = userAchievementMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAchievement>()
                            .eq(UserAchievement::getUserId, userId)
                            .eq(UserAchievement::getAchievementId, achievement.getId())
            );

            if (userAchievement == null) {
                try {
                    userAchievement = new UserAchievement();
                    userAchievement.setUserId(userId);
                    userAchievement.setAchievementId(achievement.getId());
                    userAchievement.setProgress(0);
                    userAchievement.setIsUnlocked(false);
                    userAchievementMapper.insert(userAchievement);
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    userAchievement = userAchievementMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAchievement>()
                                    .eq(UserAchievement::getUserId, userId)
                                    .eq(UserAchievement::getAchievementId, achievement.getId()));
                }
            }

            if (!userAchievement.getIsUnlocked()) {
                userAchievement.setProgress(newValue);
                if (newValue >= achievement.getConditionValue()) {
                    userAchievement.setIsUnlocked(true);
                    userAchievement.setUnlockedAt(LocalDateTime.now());

                    // 奖励积分
                    if (achievement.getPointsReward() > 0) {
                        pointUtil.addPoints(userId, "ACHIEVEMENT", achievement.getPointsReward(),
                                "解锁成就: " + achievement.getName(), "ACHIEVEMENT", achievement.getId());
                    }
                    log.info("用户 {} 解锁成就: {}", userId, achievement.getName());
                }
                userAchievementMapper.updateById(userAchievement);
            }
        }
    }
}
