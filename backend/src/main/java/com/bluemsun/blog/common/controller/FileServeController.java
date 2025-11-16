package com.bluemsun.blog.common.controller;

import com.bluemsun.blog.common.service.FileStorageService;
import com.bluemsun.blog.common.web.FileResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 通用文件访问控制器，统一从 OSS 读取资源。
 */
@RestController
@RequestMapping("/files")
public class FileServeController {

    private final FileStorageService fileStorageService;

    public FileServeController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /** GET /files/avatars/{fileName} - 从对象存储返回头像文件 */
    @GetMapping("/avatars/{fileName:.+}")
    public ResponseEntity<InputStreamResource> avatar(@PathVariable String fileName) {
        FileResource file = fileStorageService.loadFile("avatars/" + fileName, fileName);
        String contentType = StringUtils.hasText(file.contentType())
                ? file.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String encodedName = URLEncoder.encode(file.fileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        InputStreamResource resource = new InputStreamResource(file.stream());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName)
                .contentLength(file.length())
                .body(resource);
    }
}


