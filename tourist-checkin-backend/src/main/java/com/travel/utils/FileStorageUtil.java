package com.travel.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class FileStorageUtil {

    @Value("${file.storage.path:./uploads}")
    private String storagePath;

    @Value("${file.storage.access-url:/uploads/}")
    private String accessUrl;

    @Value("${base-url:}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(storagePath).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath);
            }
            log.info("文件存储路径: {}", uploadPath);
        } catch (IOException e) {
            log.error("无法创建上传目录", e);
            throw new RuntimeException("无法创建上传目录: " + e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            Path targetPath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("文件上传成功: {}", newFileName);

            // 返回可访问的URL（小程序需要HTTPS绝对路径）
            String url = accessUrl + newFileName;
            if (baseUrl != null && !baseUrl.isEmpty() && url.startsWith("/")) {
                url = baseUrl + url;
            }
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = uploadPath.resolve(filename).normalize();
            if (!filePath.startsWith(uploadPath)) {
                log.warn("路径穿越攻击被拦截: {}", fileUrl);
                return;
            }
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filename);
        } catch (IOException e) {
            log.error("文件删除失败", e);
        }
    }
}
