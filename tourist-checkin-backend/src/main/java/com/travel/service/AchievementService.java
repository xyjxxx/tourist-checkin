package com.travel.service;

import com.travel.entity.Achievement;
import com.travel.entity.UserAchievement;
import com.travel.mapper.AchievementMapper;
import com.travel.mapper.UserAchievementMapper;
import com.travel.vo.AchievementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    public List<AchievementVO> getUserAchievements(Long userId) {
        List<UserAchievement> userAchievements = userAchievementMapper.selectByUserId(userId);
        if (userAchievements.isEmpty()) return List.of();

        Map<Long, Achievement> achievementMap = achievementMapper.selectBatchIds(
                userAchievements.stream().map(UserAchievement::getAchievementId).toList()
        ).stream().collect(Collectors.toMap(Achievement::getId, a -> a));

        return userAchievements.stream()
                .map(ua -> {
                    Achievement achievement = achievementMap.get(ua.getAchievementId());
                    if (achievement == null) return null;
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
                })
                .filter(vo -> vo != null)
                .toList();
    }

    // ==================== 管理员功能 ====================

    public AchievementVO adminCreate(Achievement achievement) {
        achievementMapper.insert(achievement);
        AchievementVO vo = new AchievementVO();
        vo.setId(achievement.getId());
        vo.setCode(achievement.getCode());
        vo.setName(achievement.getName());
        vo.setDescription(achievement.getDescription());
        vo.setIcon(achievement.getIcon());
        vo.setCategory(achievement.getCategory());
        vo.setLevel(achievement.getLevel());
        vo.setPointsReward(achievement.getPointsReward());
        return vo;
    }

    public void adminUpdate(Long id, Achievement data) {
        Achievement a = achievementMapper.selectById(id);
        if (a == null) throw new com.travel.exception.BadRequestException("成就不存在");
        if (data.getName() != null) a.setName(data.getName());
        if (data.getDescription() != null) a.setDescription(data.getDescription());
        if (data.getIcon() != null) a.setIcon(data.getIcon());
        if (data.getCategory() != null) a.setCategory(data.getCategory());
        if (data.getLevel() != null) a.setLevel(data.getLevel());
        if (data.getConditionType() != null) a.setConditionType(data.getConditionType());
        if (data.getConditionValue() != null) a.setConditionValue(data.getConditionValue());
        if (data.getPointsReward() != null) a.setPointsReward(data.getPointsReward());
        if (data.getIsHidden() != null) a.setIsHidden(data.getIsHidden());
        achievementMapper.updateById(a);
    }

    @Transactional
    public void adminDelete(Long id) {
        achievementMapper.deleteById(id);
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
