package com.bluemsun.blog.module.article.service;

import com.bluemsun.blog.module.article.service.model.AttachmentSummary;
import com.bluemsun.blog.common.web.FileResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    List<AttachmentSummary> uploadAttachments(Long articleId, List<MultipartFile> files, String accessScope);

    FileResource downloadAttachment(Long attachmentId, Long userId, boolean isAdmin);

    List<AttachmentSummary> listByArticle(Long articleId);

    void removeByArticle(Long articleId);
}



