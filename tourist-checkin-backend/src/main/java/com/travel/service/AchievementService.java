package com.travel.service;

import com.travel.entity.UserAchievement;
import com.travel.mapper.AchievementMapper;
import com.travel.mapper.UserAchievementMapper;
import com.travel.vo.AchievementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    public List<AchievementVO> getUserAchievements(Long userId) {
        return userAchievementMapper.selectByUserId(userId).stream()
                .map(ua -> {
                    var achievement = achievementMapper.selectById(ua.getAchievementId());
                    AchievementVO vo = new AchievementVO();
                    vo.setId(achievement.getId());
                    vo.setCode(achievement.getCode());
                    vo.setName(achievement.getName());
                    vo.setDescription(achievement.getDescription());
                    vo.setIcon(achievement.getIcon());
                    vo.setCategory(achievement.getCategory());
                    vo.setLevel(achievement.getLevel());
                    vo.setProgress(ua.getProgress());
                    vo.setIsUnlocked(ua.getIsUnlocked());
                    vo.setUnlockedAt(ua.getUnlockedAt());
                    vo.setPointsReward(achievement.getPointsReward());
                    return vo;
                }).toList();
    }

    public List<AchievementVO> getAllDefinitions() {
        return achievementMapper.selectList(null).stream()
                .map(a -> {
                    AchievementVO vo = new AchievementVO();
                    vo.setId(a.getId());
                    vo.setCode(a.getCode());
                    vo.setName(a.getName());
                    vo.setDescription(a.getDescription());
                    vo.setIcon(a.getIcon());
                    vo.setCategory(a.getCategory());
                    vo.setLevel(a.getLevel());
                    vo.setPointsReward(a.getPointsReward());
                    vo.setIsUnlocked(false);
                    vo.setProgress(0);
                    return vo;
                }).toList();
    }
}
