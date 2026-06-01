package com.travel.service;

import com.travel.entity.Comment;
import com.travel.mapper.CommentLikeMapper;
import com.travel.mapper.CommentMapper;
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
 * 评论点赞并发幂等性测试
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class CommentServiceConcurrencyTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    private static final Long COMMENT_ID = 1L;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        Comment comment = commentMapper.selectById(COMMENT_ID);
        if (comment != null) {
            comment.setLikeCount(0);
            commentMapper.updateById(comment);
        }
        commentLikeMapper.delete(null);
    }

    @AfterEach
    void tearDown() {
        Comment comment = commentMapper.selectById(COMMENT_ID);
        if (comment != null) {
            comment.setLikeCount(0);
            commentMapper.updateById(comment);
        }
        commentLikeMapper.delete(null);
    }

    @Test
    void concurrentCommentLike_shouldBeIdempotent() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    commentService.like(USER_ID, COMMENT_ID);
                } catch (Exception e) {
                    fail("评论点赞不应抛出未捕获异常: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
        executor.shutdown();

        Comment comment = commentMapper.selectById(COMMENT_ID);
        assertNotNull(comment);
        assertEquals(1, comment.getLikeCount(), "并发点赞后 like_count 应为 1");

        // 验证点赞记录唯一性
        long likeRecords = commentLikeMapper.selectCount(null);
        assertEquals(1, likeRecords, "应仅存在 1 条评论点赞记录");
    }

    @Test
    void concurrentCommentUnlike_shouldNotGoNegative() throws InterruptedException {
        // 前置点赞
        commentService.like(USER_ID, COMMENT_ID);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    commentService.like(USER_ID, COMMENT_ID);
                } catch (Exception e) {
                    fail("取消评论点赞不应抛出未捕获异常: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
        executor.shutdown();

        Comment comment = commentMapper.selectById(COMMENT_ID);
        assertNotNull(comment);
        assertEquals(0, comment.getLikeCount(), "取消点赞后 like_count 不应为负数");
    }
}
