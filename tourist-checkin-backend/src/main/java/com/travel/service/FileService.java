package com.travel.service;

import com.travel.utils.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageUtil fileStorageUtil;

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new com.travel.exception.BadRequestException("文件不能为空");
        }
        return fileStorageUtil.uploadFile(file);
    }

    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }
}
