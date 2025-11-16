package com.bluemsun.blog.module.article.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.service.ArticleService;
import com.bluemsun.blog.module.article.service.AttachmentService;
import com.bluemsun.blog.module.article.service.model.AttachmentSummary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/articles/{articleId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final ArticleService articleService;

    public AttachmentController(AttachmentService attachmentService, ArticleService articleService) {
        this.attachmentService = attachmentService;
        this.articleService = articleService;
    }

    /** POST /articles/{articleId}/attachments - 为文章上传附件 */
    @SaCheckLogin
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<AttachmentSummary>> upload(@PathVariable Long articleId,
                                                       @RequestParam(value = "accessScope", defaultValue = "PUBLIC") String accessScope,
                                                       @RequestPart("files") List<MultipartFile> files) {
        Article article = articleService.getById(articleId);
        if (article == null) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        if (!isAdmin && !article.getAuthorId().equals(userId)) {
            throw new BizException(ApiCode.FORBIDDEN, "仅作者或管理员可上传附件");
        }

        List<AttachmentSummary> summaries = attachmentService.uploadAttachments(articleId, files, accessScope);
        return ApiResponse.success(summaries, "附件上传成功");
    }
}


