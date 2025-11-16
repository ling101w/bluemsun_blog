package com.bluemsun.blog.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.common.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户头像上传接口。
 */
@RestController
@RequestMapping("/users/avatar")
public class UserAvatarController {

    private final FileStorageService fileStorageService;

    public UserAvatarController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /** POST /users/avatar - 上传新的头像图片 */
    @SaCheckLogin
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> upload(@RequestPart("file") MultipartFile file) {
        // 验证图片文件
        fileStorageService.validateImageFile(file);

        // 生成新文件名和 OSS 键
        String newFileName = fileStorageService.generateFileName(file.getOriginalFilename());
        String key = "avatars/" + newFileName;

        // 上传到 OSS
        fileStorageService.uploadToOss(file, key);

        // 构建访问 URL
        String url = fileStorageService.buildPublicUrl(key);

        return ApiResponse.success(Map.of(
                "url", url,
                "fileName", newFileName
        ), "上传成功");
    }
}


