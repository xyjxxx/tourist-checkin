package com.travel.service;

import com.travel.mapper.CheckInMapper;
import com.travel.vo.ChartVO;
import com.travel.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CheckInMapper checkInMapper;

    public UserReportVO getUserReport(Long userId, int year) {
        // 从打卡数据聚合计算年度报告
        UserReportVO report = new UserReportVO();
        report.setTotalCheckIns(checkInMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.travel.entity.CheckIn>()
                        .eq(com.travel.entity.CheckIn::getUserId, userId)));
        // 实际生产环境需要用 SQL 聚合统计城市数、点赞数等
        report.setTotalCities(0L);
        report.setTotalLikes(0L);
        report.setTotalDays(0L);
        report.setLongestStreak(0);
        report.setMostActiveMonth("-");
        report.setMostActiveCity("-");
        report.setFavoriteCategory("-");
        return report;
    }

    public ChartVO getMonthlyTrend(Long userId, int year) {
        ChartVO chart = new ChartVO();
        chart.setLabels(List.of("1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"));
        chart.setSeriesName("打卡次数");
        // 查询当月打卡数填充 values
        chart.setValues(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        return chart;
    }
}
