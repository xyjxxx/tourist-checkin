package com.travel.controller;

import com.travel.service.FileService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
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

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        requireUserId(request);
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
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4) return false;
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
            // PNG: 89 50 4E 47
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
            // GIF: 47 49 46 38
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) return true;
            // WEBP: RIFF....WEBP
            if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                && read >= 12 && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
