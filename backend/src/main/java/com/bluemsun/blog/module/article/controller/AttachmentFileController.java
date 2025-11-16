package com.bluemsun.blog.module.article.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bluemsun.blog.common.web.FileResource;
import com.bluemsun.blog.module.article.service.AttachmentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentFileController {

    private final AttachmentService attachmentService;

    public AttachmentFileController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /** GET /attachments/{attachmentId} - 下载附件文件 */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long attachmentId) {
        Long userId = null;
        boolean isAdmin = false;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsLong();
            isAdmin = StpUtil.hasRole("ADMIN");
        }

        FileResource file = attachmentService.downloadAttachment(attachmentId, userId, isAdmin);
        InputStreamResource resource = new InputStreamResource(file.stream());
        String encodedName = URLEncoder.encode(file.fileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(file.contentType());
        } catch (IllegalArgumentException ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(file.length())
                .body(resource);
    }
}


