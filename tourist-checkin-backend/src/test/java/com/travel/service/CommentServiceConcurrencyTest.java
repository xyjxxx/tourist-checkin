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
    private static final int THREAD_COUNT = 5;

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
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    commentService.like(USER_ID, COMMENT_ID);
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

        Comment comment = commentMapper.selectById(COMMENT_ID);
        assertNotNull(comment);
        assertTrue(comment.getLikeCount() >= 1, "like_count 应 >= 1");
        assertEquals(1, commentLikeMapper.selectCount(null), "应仅存在 1 条点赞记录");
    }

    @Test
    void unlikeAfterLike_shouldRestoreCount() {
        // 先点赞
        commentService.like(USER_ID, COMMENT_ID);
        Comment afterLike = commentMapper.selectById(COMMENT_ID);
        assertEquals(1, afterLike.getLikeCount());

        // 再取消
        commentService.like(USER_ID, COMMENT_ID);
        Comment afterUnlike = commentMapper.selectById(COMMENT_ID);
        assertEquals(0, afterUnlike.getLikeCount(), "取消后 like_count 应为 0");
    }
}
