package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.entity.PointRecord;
import com.travel.entity.UserPoint;
import com.travel.mapper.PointRecordMapper;
import com.travel.mapper.UserPointMapper;
import com.travel.vo.PointRecordVO;
import com.travel.vo.UserPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointMapper userPointMapper;
    private final PointRecordMapper pointRecordMapper;

    public UserPointVO getUserPoints(Long userId) {
        LambdaQueryWrapper<UserPoint> q = new LambdaQueryWrapper<>();
        q.eq(UserPoint::getUserId, userId);
        UserPoint up = userPointMapper.selectOne(q);
        if (up == null) {
            UserPointVO vo = new UserPointVO();
            vo.setTotalPoints(0);
            vo.setCurrentPoints(0);
            vo.setLevel(0);
            vo.setLevelName("普通用户");
            vo.setNextLevelPoints(100);
            return vo;
        }
        UserPointVO vo = new UserPointVO();
        vo.setTotalPoints(up.getTotalPoints());
        vo.setCurrentPoints(up.getCurrentPoints());
        vo.setLevel(up.getLevel());
        vo.setLevelName(getLevelName(up.getLevel()));
        vo.setNextLevelPoints(getNextLevelPoints(up.getTotalPoints()));
        return vo;
    }

    public List<PointRecordVO> getRecords(Long userId, int page, int size) {
        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointRecord::getUserId, userId)
               .orderByDesc(PointRecord::getCreatedAt);

        Page<PointRecord> p = new Page<>(page, size);
        Page<PointRecord> result = pointRecordMapper.selectPage(p, wrapper);

        return result.getRecords().stream().map(r -> {
            PointRecordVO vo = new PointRecordVO();
            vo.setId(r.getId());
            vo.setType(r.getType());
            vo.setPoints(r.getPoints());
            vo.setDescription(r.getDescription());
            vo.setBalanceAfter(r.getBalanceAfter());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).toList();
    }

    private String getLevelName(int level) {
        return switch (level) {
            case 4 -> "铂金会员";
            case 3 -> "黄金会员";
            case 2 -> "白银会员";
            case 1 -> "青铜会员";
            default -> "普通用户";
        };
    }

    private int getNextLevelPoints(int total) {
        if (total >= 5000) return 0;
        if (total >= 2000) return 5000;
        if (total >= 500) return 2000;
        if (total >= 100) return 500;
        return 100;
    }
}
