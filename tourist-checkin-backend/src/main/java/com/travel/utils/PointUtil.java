package com.travel.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.PointRecord;
import com.travel.entity.UserPoint;
import com.travel.mapper.PointRecordMapper;
import com.travel.mapper.UserPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PointUtil {

    private final UserPointMapper userPointMapper;
    private final PointRecordMapper pointRecordMapper;

    /** 积分规则 */
    public static final int POINTS_CHECK_IN = 5;
    public static final int POINTS_LIKE = 2;
    public static final int POINTS_LIKE_RECEIVED = 2;
    public static final int POINTS_COMMENT = 3;
    public static final int POINTS_FOLLOW = 1;
    public static final int POINTS_DAILY = 3;

    @Transactional
    public void addPoints(Long userId, String type, int points, String description,
                          String refType, Long refId) {
        // 获取或创建积分账户（userId不是主键，需要用LambdaQueryWrapper查询）
        LambdaQueryWrapper<UserPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPoint::getUserId, userId);
        UserPoint userPoint = userPointMapper.selectOne(wrapper);
        if (userPoint == null) {
            userPoint = new UserPoint();
            userPoint.setUserId(userId);
            userPoint.setTotalPoints(0);
            userPoint.setCurrentPoints(0);
            userPoint.setLevel(0);
            try {
                userPointMapper.insert(userPoint);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发创建冲突，重新查询
                userPoint = userPointMapper.selectOne(wrapper);
            }
        }

        // 原子增加积分，避免并发丢失更新
        userPointMapper.addPointsAtomic(userId, points);

        // 重新查询最新余额
        userPoint = userPointMapper.selectOne(wrapper);

        // 检查等级升级
        int newLevel = calculateLevel(userPoint.getTotalPoints());
        if (newLevel > userPoint.getLevel()) {
            userPoint.setLevel(newLevel);
            userPointMapper.updateById(userPoint);
        }

        // 记录流水（基于原子更新后的真实余额）
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setPoints(points);
        record.setDescription(description);
        record.setRefType(refType);
        record.setRefId(refId);
        record.setBalanceAfter(userPoint.getCurrentPoints());
        pointRecordMapper.insert(record);
    }

    private int calculateLevel(int totalPoints) {
        if (totalPoints >= 5000) return 4;  // 铂金
        if (totalPoints >= 2000) return 3;  // 黄金
        if (totalPoints >= 500) return 2;   // 白银
        if (totalPoints >= 100) return 1;   // 青铜
        return 0;                            // 普通
    }
}
