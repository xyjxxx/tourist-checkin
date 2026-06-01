package com.travel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.storage.path:./uploads}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地存储路径
        Path uploadPath = Paths.get(storagePath).toAbsolutePath().normalize();
        String uploadPathStr = uploadPath.toString().replace("\\", "/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPathStr + "/");
    }
}
