package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.*;
import com.travel.mapper.*;
import com.travel.vo.ChartVO;
import com.travel.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CheckInMapper checkInMapper;
    private final UserMapper userMapper;
    private final TravelNoteMapper travelNoteMapper;
    private final TripPlanMapper tripPlanMapper;
    private final CommentMapper commentMapper;
    private final LocationMapper locationMapper;

    public UserReportVO getUserReport(Long userId, int year) {
        UserReportVO report = new UserReportVO();
        java.time.LocalDateTime yearStart = LocalDate.of(year, 1, 1).atStartOfDay();
        java.time.LocalDateTime yearEnd = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        // 年度打卡总数
        report.setTotalCheckIns(checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, yearStart)
                        .le(CheckIn::getCheckInTime, yearEnd)));

        // 去过的城市数
        Long distinctCities = checkInMapper.selectDistinctLocationCountByUser(userId, yearStart, yearEnd);
        report.setTotalCities(distinctCities != null ? distinctCities : 0L);

        // 获得的点赞数（通过打卡的 like_count 求和）
        List<CheckIn> userCheckIns = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, yearStart)
                        .le(CheckIn::getCheckInTime, yearEnd));
        long totalLikes = userCheckIns.stream().mapToLong(ci -> ci.getLikeCount() != null ? ci.getLikeCount() : 0).sum();
        report.setTotalLikes(totalLikes);

        // 打卡天数
        long distinctDays = userCheckIns.stream()
                .filter(ci -> ci.getCheckInTime() != null)
                .map(ci -> ci.getCheckInTime().toLocalDate())
                .distinct().count();
        report.setTotalDays(distinctDays);

        // longestStreak: consecutive days with check-ins
        List<LocalDate> checkInDates = userCheckIns.stream()
                .filter(ci -> ci.getCheckInTime() != null)
                .map(ci -> ci.getCheckInTime().toLocalDate())
                .distinct().sorted().toList();
        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate prevDate = null;
        for (LocalDate d : checkInDates) {
            if (prevDate != null && ChronoUnit.DAYS.between(prevDate, d) == 1) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            if (currentStreak > longestStreak) longestStreak = currentStreak;
            prevDate = d;
        }
        report.setLongestStreak(longestStreak);

        // mostActiveMonth
        Map<Integer, Long> monthCounts = userCheckIns.stream()
                .filter(ci -> ci.getCheckInTime() != null)
                .collect(Collectors.groupingBy(ci -> ci.getCheckInTime().getMonthValue(), Collectors.counting()));
        report.setMostActiveMonth(monthCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + "月").orElse("-"));

        // mostActiveCity
        Map<String, Long> cityCounts = userCheckIns.stream()
                .filter(ci -> ci.getLocationName() != null && !ci.getLocationName().isEmpty())
                .collect(Collectors.groupingBy(CheckIn::getLocationName, Collectors.counting()));
        report.setMostActiveCity(cityCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-"));

        // favoriteCategory: based on location category
        List<Long> locationIds = userCheckIns.stream()
                .filter(ci -> ci.getLocationId() != null)
                .map(CheckIn::getLocationId).distinct().toList();
        if (!locationIds.isEmpty()) {
            List<Location> locations = locationMapper.selectBatchIds(locationIds);
            Map<String, Long> categoryCounts = locations.stream()
                    .filter(l -> l.getCategory() != null && !l.getCategory().isEmpty())
                    .collect(Collectors.groupingBy(Location::getCategory, Collectors.counting()));
            report.setFavoriteCategory(categoryCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("-"));
        } else {
            report.setFavoriteCategory("-");
        }

        return report;
    }

    public ChartVO getMonthlyTrend(Long userId, int year) {
        ChartVO chart = new ChartVO();
        chart.setLabels(List.of("1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"));
        chart.setSeriesName("打卡次数");

        // 按月统计打卡数
        List<CheckIn> yearCheckIns = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, LocalDate.of(year, 1, 1).atStartOfDay())
                        .le(CheckIn::getCheckInTime, LocalDate.of(year, 12, 31).atTime(23, 59, 59)));

        int[] monthlyCounts = new int[12];
        for (CheckIn ci : yearCheckIns) {
            if (ci.getCheckInTime() != null) {
                monthlyCounts[ci.getCheckInTime().getMonthValue() - 1]++;
            }
        }
        java.util.List<Integer> values = new java.util.ArrayList<>();
        for (int count : monthlyCounts) values.add(count);
        chart.setValues(values);
        return chart;
    }

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)));
        stats.put("totalCheckIns", checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getDeleted, 0)));
        stats.put("totalTravelNotes", travelNoteMapper.selectCount(
                new LambdaQueryWrapper<TravelNote>().ne(TravelNote::getStatus, 0)));
        stats.put("totalTripPlans", tripPlanMapper.selectCount(
                new LambdaQueryWrapper<TripPlan>().ne(TripPlan::getStatus, 0)));
        stats.put("totalComments", commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().ne(Comment::getStatus, 0)));
        stats.put("totalLocations", locationMapper.selectCount(
                new LambdaQueryWrapper<Location>().eq(Location::getDeleted, 0)));

        // 今日数据
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime todayStart = today.atStartOfDay();
        stats.put("todayNewUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreatedAt, todayStart)));
        stats.put("todayCheckIns", checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>()
                        .ge(CheckIn::getCheckInTime, todayStart)));
        return stats;
    }
}
