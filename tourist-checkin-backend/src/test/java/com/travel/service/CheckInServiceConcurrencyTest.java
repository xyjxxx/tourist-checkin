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

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class CheckInServiceConcurrencyTest {

    @Autowired
    private CheckInService checkInService;
    @Autowired
    private CheckInLikeMapper checkInLikeMapper;

    private static final Long CHECK_IN_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final int THREAD_COUNT = 5; // H2 兼容的并发数

    @BeforeEach
    void setUp() {
        checkInLikeMapper.delete(new LambdaQueryWrapper<CheckInLike>()
                .eq(CheckInLike::getCheckInId, CHECK_IN_ID));
    }

    @AfterEach
    void tearDown() {
        checkInLikeMapper.delete(new LambdaQueryWrapper<CheckInLike>()
                .eq(CheckInLike::getCheckInId, CHECK_IN_ID));
    }

    @Test
    void concurrentLike_shouldBeIdempotent() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    checkInService.likeCheckIn(USER_ID, CHECK_IN_ID);
                } catch (Exception e) {
                    // 并发冲突被捕获，正常
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        Long likeCount = checkInLikeMapper.selectCount(
                new LambdaQueryWrapper<CheckInLike>().eq(CheckInLike::getCheckInId, CHECK_IN_ID));
        assertEquals(1, likeCount, "并发点赞应仅产生 1 条记录");
    }

    @Test
    void unlikeAfterLike_shouldRemoveRecord() {
        // 先点赞
        checkInService.likeCheckIn(USER_ID, CHECK_IN_ID);
        assertEquals(1, checkInLikeMapper.selectCount(
                new LambdaQueryWrapper<CheckInLike>().eq(CheckInLike::getCheckInId, CHECK_IN_ID)));

        // 再取消（toggle）
        checkInService.likeCheckIn(USER_ID, CHECK_IN_ID);
        assertEquals(0, checkInLikeMapper.selectCount(
                new LambdaQueryWrapper<CheckInLike>().eq(CheckInLike::getCheckInId, CHECK_IN_ID)));
    }
}
