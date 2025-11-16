package com.bluemsun.blog.module.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.article.dto.ArticleQueryDTO;
import com.bluemsun.blog.module.article.service.ArticleService;
import com.bluemsun.blog.module.article.vo.ArticleListVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/admin/articles")
@SaCheckRole("ADMIN")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 管理后台 - 文章分页列表
     */
    /** GET /admin/articles - 管理后台分页查询文章 */
    @GetMapping
    public ApiResponse<IPage<ArticleListVO>> page(@Valid ArticleQueryDTO queryDTO) {
        return ApiResponse.success(articleService.pageArticles(queryDTO));
    }

    /** POST /admin/articles/{id}/publish - 强制发布文章 */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        articleService.publishArticle(id);
        return ApiResponse.success(null, "文章已发布");
    }

    /** POST /admin/articles/{id}/offline - 下线文章 */
    @PostMapping("/{id}/offline")
    public ApiResponse<Void> offline(@PathVariable Long id) {
        articleService.offlineArticle(id);
        return ApiResponse.success(null, "文章已下线");
    }

    /** POST /admin/articles/{id}/pin - 将文章设置为公告 */
    @PostMapping("/{id}/pin")
    public ApiResponse<Void> pin(@PathVariable Long id) {
        articleService.pinArticle(id);
        return ApiResponse.success(null, "已设为公告");
    }

    /** POST /admin/articles/{id}/unpin - 取消公告文章 */
    @PostMapping("/{id}/unpin")
    public ApiResponse<Void> unpin(@PathVariable Long id) {
        articleService.unpinArticle(id);
        return ApiResponse.success(null, "已取消公告");
    }
}
