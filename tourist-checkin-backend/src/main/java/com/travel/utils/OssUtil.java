package com.travel.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class OssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFileName = "travel/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, newFileName, file.getInputStream());
            ossClient.putObject(putObjectRequest);

            return "https://" + bucketName + "." + endpoint + "/" + newFileName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.indexOf("travel/"));
            ossClient.deleteObject(bucketName, key);
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }
}
