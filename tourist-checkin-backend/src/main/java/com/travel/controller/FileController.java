package com.travel.controller;

import com.travel.service.FileService;
import com.travel.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过10MB");
        }

        // 校验文件类型（扩展名 + 内容头魔数）
        String originalFilename = file.getOriginalFilename();
        if (!isValidImageFile(originalFilename)) {
            return Result.error("只能上传图片文件(JPG/PNG/GIF/WEBP)");
        }
        if (!isValidImageContent(file)) {
            return Result.error("文件内容不是有效的图片格式");
        }

        String url = fileService.uploadFile(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return Result.success(result);
    }

    @PostMapping("/upload/batch")
    public Result<Map<String, List<String>>> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        if (files.size() > 9) {
            return Result.error("一次最多上传9张图片");
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error("单个文件大小不能超过10MB");
            }
            if (!isValidImageFile(file.getOriginalFilename())) {
                return Result.error("只能上传图片文件");
            }
            if (!isValidImageContent(file)) {
                return Result.error("文件内容不是有效的图片格式");
            }
        }

        List<String> urls = fileService.uploadFiles(files);
        Map<String, List<String>> result = new HashMap<>();
        result.put("urls", urls);
        return Result.success(result);
    }

    private boolean isValidImageFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return false;
        }
        String extension = filename.substring(lastDotIndex).toLowerCase();
        return Arrays.asList(ALLOWED_EXTENSIONS).contains(extension);
    }

    private boolean isValidImageContent(MultipartFile file) {
        try (java.io.InputStream is = file.getInputStream()) {
            // ImageIO 能成功读取即证明是真实图片（会校验文件头魔数）
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(is);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
