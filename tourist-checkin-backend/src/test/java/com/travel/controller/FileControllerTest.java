package com.travel.controller;

import com.travel.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FileControllerTest {

    private final FileController fileController = new FileController(
            new FileControllerTest.FakeFileService()
    );

    /**
     * BUG-002 回归测试：伪装成图片的文本文件应被内容校验拦截
     */
    @Test
    void isValidImageContent_shouldRejectFakeImage() throws Exception {
        Method method = FileController.class.getDeclaredMethod("isValidImageContent",
                org.springframework.web.multipart.MultipartFile.class);
        method.setAccessible(true);

        // 构造一个名为 test.jpg 但实际内容是纯文本的文件
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file", "evil.jpg", "image/jpeg", "this is not an image".getBytes()
        );

        boolean result = (boolean) method.invoke(fileController, fakeImage);
        assertFalse(result, "非图片内容应被识别并拒绝");
    }

    @Test
    void isValidImageContent_shouldAcceptRealImage() throws Exception {
        Method method = FileController.class.getDeclaredMethod("isValidImageContent",
                org.springframework.web.multipart.MultipartFile.class);
        method.setAccessible(true);

        // 1x1 像素的透明 PNG（最小的合法 PNG）
        byte[] realPng = {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
                0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00,
                0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
        MockMultipartFile realImage = new MockMultipartFile(
                "file", "real.png", "image/png", realPng
        );

        boolean result = (boolean) method.invoke(fileController, realImage);
        assertTrue(result, "合法 PNG 应通过内容校验");
    }

    static class FakeFileService extends FileService {
        FakeFileService() {
            super(null);
        }
    }
}
