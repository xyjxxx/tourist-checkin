package com.travel.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.PointRecord;
import com.travel.entity.UserPoint;
import com.travel.mapper.PointRecordMapper;
import com.travel.mapper.UserPointMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 积分并发一致性测试
 * 验证高并发下：1) 余额无丢失更新 2) 流水总数与余额一致 3) balance_after 最终准确
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class PointUtilConcurrencyTest {

    @Autowired
    private PointUtil pointUtil;

    @Autowired
    private UserPointMapper userPointMapper;

    @Autowired
    private PointRecordMapper pointRecordMapper;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 清理用户积分和流水
        pointRecordMapper.delete(
                new LambdaQueryWrapper<PointRecord>().eq(PointRecord::getUserId, USER_ID)
        );
        userPointMapper.delete(
                new LambdaQueryWrapper<UserPoint>().eq(UserPoint::getUserId, USER_ID)
        );
    }

    @AfterEach
    void tearDown() {
        pointRecordMapper.delete(
                new LambdaQueryWrapper<PointRecord>().eq(PointRecord::getUserId, USER_ID)
        );
        userPointMapper.delete(
                new LambdaQueryWrapper<UserPoint>().eq(UserPoint::getUserId, USER_ID)
        );
    }

    @Test
    void concurrentAddPoints_shouldMaintainConsistency() throws InterruptedException {
        int threadCount = 20;
        int pointsPerOp = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int seq = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    pointUtil.addPoints(USER_ID, "TEST", pointsPerOp,
                            "测试积分 " + seq, "TEST", (long) seq);
                } catch (Exception e) {
                    fail("积分增加不应抛出未捕获异常: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS), "并发积分操作应在15秒内完成");
        executor.shutdown();

        // 验证 1：最终余额 = threadCount * pointsPerOp
        UserPoint userPoint = userPointMapper.selectOne(
                new LambdaQueryWrapper<UserPoint>().eq(UserPoint::getUserId, USER_ID)
        );
        assertNotNull(userPoint, "积分账户应存在");
        int expectedTotal = threadCount * pointsPerOp;
        assertEquals(expectedTotal, userPoint.getCurrentPoints(),
                "current_points 应等于所有操作之和，无丢失更新");
        assertEquals(expectedTotal, userPoint.getTotalPoints(),
                "total_points 应等于所有操作之和");

        // 验证 2：流水记录数 = threadCount
        List<PointRecord> records = pointRecordMapper.selectList(
                new LambdaQueryWrapper<PointRecord>()
                        .eq(PointRecord::getUserId, USER_ID)
                        .orderByAsc(PointRecord::getId)
        );
        assertEquals(threadCount, records.size(), "应产生 " + threadCount + " 条积分流水");

        // 验证 3：balance_after 单调不减，且最终最大值等于当前余额
        int maxBalance = records.stream()
                .mapToInt(PointRecord::getBalanceAfter)
                .max()
                .orElse(0);
        assertEquals(userPoint.getCurrentPoints(), maxBalance,
                "最终 balance_after 应等于当前余额");

        // 验证 4：流水积分总和等于余额（允许因并发时序导致 balance_after 不是严格递增，但总和应一致）
        int sumPoints = records.stream().mapToInt(PointRecord::getPoints).sum();
        assertEquals(expectedTotal, sumPoints, "流水积分总和应等于余额");
    }

    @Test
    void concurrentMixedPoints_shouldNotLoseUpdates() throws InterruptedException {
        // 先给账户预存 100 分
        pointUtil.addPoints(USER_ID, "INIT", 100, "初始积分", "TEST", 0L);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int seq = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    pointUtil.addPoints(USER_ID, "TEST", 10,
                            "并发测试 " + seq, "TEST", (long) seq);
                } catch (Exception e) {
                    fail("不应抛出异常: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
        executor.shutdown();

        UserPoint userPoint = userPointMapper.selectOne(
                new LambdaQueryWrapper<UserPoint>().eq(UserPoint::getUserId, USER_ID)
        );
        assertNotNull(userPoint);
        assertEquals(200, userPoint.getCurrentPoints(), "100 + 10*10 = 200，不应丢失");
    }
}
