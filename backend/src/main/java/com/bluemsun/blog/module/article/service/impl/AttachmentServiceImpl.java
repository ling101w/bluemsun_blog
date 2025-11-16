package com.bluemsun.blog.module.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.common.service.FileStorageService;
import com.bluemsun.blog.common.web.FileResource;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.entity.ArticleContent;
import com.bluemsun.blog.module.article.entity.Attachment;
import com.bluemsun.blog.module.article.mapper.ArticleContentMapper;
import com.bluemsun.blog.module.article.mapper.ArticleMapper;
import com.bluemsun.blog.module.article.mapper.AttachmentMapper;
import com.bluemsun.blog.module.article.service.AttachmentService;
import com.bluemsun.blog.module.article.service.model.AttachmentSummary;
import com.bluemsun.blog.module.order.entity.Order;
import com.bluemsun.blog.module.order.mapper.OrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AttachmentServiceImpl implements AttachmentService {


    private final AttachmentMapper attachmentMapper;
    private final ArticleMapper articleMapper;
    private final ArticleContentMapper articleContentMapper;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;

    public AttachmentServiceImpl(AttachmentMapper attachmentMapper,
                                 ArticleMapper articleMapper,
                                 ArticleContentMapper articleContentMapper,
                                 OrderMapper orderMapper,
                                 ObjectMapper objectMapper,
                                 FileStorageService fileStorageService) {
        this.attachmentMapper = attachmentMapper;
        this.articleMapper = articleMapper;
        this.articleContentMapper = articleContentMapper;
        this.orderMapper = orderMapper;
        this.objectMapper = objectMapper;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AttachmentSummary> uploadAttachments(Long articleId, List<MultipartFile> files, String accessScope) {
        if (files == null || files.isEmpty()) {
            throw new BizException(ApiCode.BAD_REQUEST, "请选择上传文件");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        String normalizedScope = normalizeScope(accessScope);

        List<AttachmentSummary> summaries = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            // 使用统一服务验证文件
            fileStorageService.validateFile(file);
            
            String originalName = file.getOriginalFilename();
            String newFileName = fileStorageService.generateFileName(originalName);
            String key = "articles/" + articleId + "/" + newFileName;
            
            // 使用统一服务上传文件
            fileStorageService.uploadToOss(file, key);

            Attachment entity = new Attachment();
            entity.setArticleId(articleId);
            entity.setFileName(newFileName);
            entity.setOriginalName(originalName);
            entity.setContentType(file.getContentType());
            entity.setFileSize(file.getSize());
            entity.setStoragePath(key);
            entity.setAccessScope(normalizedScope);
            attachmentMapper.insert(entity);

            String downloadUrl;
            if ("PUBLIC".equalsIgnoreCase(normalizedScope)) {
                downloadUrl = fileStorageService.buildPublicUrl(key);
            } else {
                downloadUrl = "/attachments/" + entity.getId();
            }
            entity.setDownloadUrl(downloadUrl);
            attachmentMapper.updateById(entity);

            summaries.add(toSummary(entity));
        }

        refreshAttachmentSummary(articleId);
        return summaries;
    }

    @Override
    public FileResource downloadAttachment(Long attachmentId, Long userId, boolean isAdmin) {
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || Boolean.TRUE.equals(attachment.getDeleted())) {
            throw new BizException(ApiCode.NOT_FOUND, "附件不存在");
        }

        Article article = articleMapper.selectById(attachment.getArticleId());
        if (article == null) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }

        boolean isAuthor = userId != null && userId.equals(article.getAuthorId());
        if ("PAID_ONLY".equalsIgnoreCase(attachment.getAccessScope())) {
            if (!isAdmin && !isAuthor) {
                boolean purchased = false;
                if (userId != null) {
                    purchased = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                            .eq(Order::getUserId, userId)
                            .eq(Order::getArticleId, article.getId())
                            .eq(Order::getStatus, "PAID")) > 0;
                }
                if (!purchased) {
                    throw new BizException(ApiCode.FORBIDDEN, "请先购买后再下载附件");
                }
            }
        }

        String key = attachment.getStoragePath();
        String fileName = StringUtils.isNotBlank(attachment.getOriginalName()) ? attachment.getOriginalName() : attachment.getFileName();
        FileResource file = fileStorageService.loadFile(key, fileName);
        return file;
    }

    @Override
    public List<AttachmentSummary> listByArticle(Long articleId) {
        List<Attachment> list = attachmentMapper.selectList(Wrappers.<Attachment>lambdaQuery()
                .eq(Attachment::getArticleId, articleId));
        List<AttachmentSummary> summaries = new ArrayList<>();
        for (Attachment attachment : list) {
            summaries.add(toSummary(attachment));
        }
        return summaries;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByArticle(Long articleId) {
        List<Attachment> list = attachmentMapper.selectList(Wrappers.<Attachment>lambdaQuery()
                .eq(Attachment::getArticleId, articleId));
        for (Attachment attachment : list) {
            String key = attachment.getStoragePath();
            // 使用统一服务删除文件
            fileStorageService.deleteFromOss(key);
        }
        attachmentMapper.delete(Wrappers.<Attachment>lambdaQuery().eq(Attachment::getArticleId, articleId));
        refreshAttachmentSummary(articleId);
    }

    private void refreshAttachmentSummary(Long articleId) {
        List<AttachmentSummary> summaries = listByArticle(articleId);
        String json;
        try {
            json = objectMapper.writeValueAsString(summaries);
        } catch (JsonProcessingException e) {
            throw new BizException(ApiCode.SERVER_ERROR, "附件信息处理失败");
        }
        ArticleContent content = articleContentMapper.selectById(articleId);
        if (content != null) {
            content.setAttachments(json);
            content.setUpdateTime(LocalDateTime.now());
            articleContentMapper.updateById(content);
        }
    }


    private String normalizeScope(String scope) {
        String normalized = StringUtils.defaultIfBlank(scope, "PUBLIC").trim().toUpperCase(Locale.ROOT);
        if (!"PUBLIC".equals(normalized) && !"PAID_ONLY".equals(normalized)) {
            throw new BizException(ApiCode.BAD_REQUEST, "附件权限类型不合法");
        }
        return normalized;
    }

    private AttachmentSummary toSummary(Attachment attachment) {
        String name = StringUtils.isNotBlank(attachment.getOriginalName()) ? attachment.getOriginalName() : attachment.getFileName();
        String url = StringUtils.defaultIfBlank(attachment.getDownloadUrl(), "/attachments/" + attachment.getId());
        boolean restrict = "PAID_ONLY".equalsIgnoreCase(attachment.getAccessScope());
        return new AttachmentSummary(attachment.getId(), name, url, restrict, attachment.getFileSize());
    }

}


