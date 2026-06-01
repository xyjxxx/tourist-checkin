package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.CheckInLike;
import com.travel.mapper.CheckInLikeMapper;
import com.travel.mapper.CheckInMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 并发点赞幂等性测试
 * 验证高并发下：1) 不产生重复点赞记录 2) 最终数据一致性
 * check_in 表无 like_count 冗余字段，点赞数通过 COUNT() 动态计算
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class CheckInServiceConcurrencyTest {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CheckInLikeMapper checkInLikeMapper;

    private static final Long CHECK_IN_ID = 1L;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 确保初始状态：无点赞记录
        checkInLikeMapper.delete(
                new LambdaQueryWrapper<CheckInLike>()
                        .eq(CheckInLike::getCheckInId, CHECK_IN_ID)
        );
    }

    @AfterEach
    void tearDown() {
        checkInLikeMapper.delete(
                new LambdaQueryWrapper<CheckInLike>()
                        .eq(CheckInLike::getCheckInId, CHECK_IN_ID)
        );
    }

    @Test
    void concurrentLike_shouldBeIdempotentAndCountCorrect() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    checkInService.likeCheckIn(USER_ID, CHECK_IN_ID);
                } catch (Exception e) {
                    // 并发唯一索引冲突已被捕获，不应抛到此处
                    fail("点赞过程中不应抛出未捕获异常: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 同时开始
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS), "并发任务应在15秒内完成");
        executor.shutdown();

        // 验证：数据库中仅存在 1 条点赞记录
        Long likeCount = checkInLikeMapper.selectCount(
                new LambdaQueryWrapper<CheckInLike>()
                        .eq(CheckInLike::getCheckInId, CHECK_IN_ID)
        );
        assertEquals(1, likeCount, "并发点赞应仅产生 1 条点赞记录（幂等）");
    }

    @Test
    void concurrentUnlike_shouldRemoveLikeRecord() throws InterruptedException {
        // 前置：先点赞
        checkInService.likeCheckIn(USER_ID, CHECK_IN_ID);
        Long before = checkInLikeMapper.selectCount(
                new LambdaQueryWrapper<CheckInLike>()
                        .eq(CheckInLike::getCheckInId, CHECK_IN_ID)
        );
        assertEquals(1, before);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    checkInService.likeCheckIn(USER_ID, CHECK_IN_ID);
                } catch (Exception e) {
                    fail("取消点赞过程中不应抛出未捕获异常: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
        executor.shutdown();

        // 验证：最终无点赞记录
        Long after = checkInLikeMapper.selectCount(
                new LambdaQueryWrapper<CheckInLike>()
                        .eq(CheckInLike::getCheckInId, CHECK_IN_ID)
        );
        assertEquals(0, after, "取消点赞后应无点赞记录");
    }
}
