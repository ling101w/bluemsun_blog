package com.bluemsun.blog.module.article.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.article.dto.ArticleCreateDTO;
import com.bluemsun.blog.module.article.dto.ArticleQueryDTO;
import com.bluemsun.blog.module.article.dto.ArticleUpdateDTO;
import com.bluemsun.blog.module.article.service.ArticleService;
import com.bluemsun.blog.module.article.vo.ArticleDetailVO;
import com.bluemsun.blog.module.article.vo.ArticleListVO;
import com.bluemsun.blog.module.article.vo.HotCreatorVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /** GET /articles - 分页获取文章列表 */
    @GetMapping
    public ApiResponse<IPage<ArticleListVO>> page(@Valid ArticleQueryDTO queryDTO) {
        return ApiResponse.success(articleService.pageArticles(queryDTO));
    }

    /** GET /articles/{id} - 查询文章详情（可选包含草稿） */
    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailVO> detail(@PathVariable("id") Long id,
                                               @RequestParam(value = "draft", defaultValue = "false") boolean includeDraft) {
        Long viewerId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        boolean isAdmin = StpUtil.isLogin() && StpUtil.hasRole("ADMIN");
        return ApiResponse.success(articleService.getArticleDetail(id, includeDraft, viewerId, isAdmin));
    }

    /** GET /articles/hot - 获取热门文章列表 */
    @GetMapping("/hot")
    public ApiResponse<List<ArticleListVO>> hot(@RequestParam(defaultValue = "10") @Min(1) int limit) {
        return ApiResponse.success(articleService.listHot(Math.min(limit, 20)));
    }

    /** GET /articles/creators/hot - 获取热门创作者列表 */
    @GetMapping("/creators/hot")
    public ApiResponse<List<HotCreatorVO>> hotCreators(@RequestParam(defaultValue = "5") @Min(1) int limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        return ApiResponse.success(articleService.listHotCreators(size));
    }

    /** GET /articles/announcement - 获取置顶公告文章 */
    @GetMapping("/announcement")
    public ApiResponse<ArticleDetailVO> announcement() {
        return ApiResponse.success(articleService.getPinnedAnnouncement());
    }

    /**
     * 创建文章：支持“保存草稿”与“直接发布”两种行为。
     */
    /** POST /articles - 创建文章（支持保存草稿或直接发布） */
    @SaCheckLogin
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ArticleCreateDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(articleService.createArticle(userId, request));
    }

    /** PUT /articles/{id} - 更新文章内容 */
    @SaCheckLogin
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody ArticleUpdateDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.updateArticle(userId, id, request);
        return ApiResponse.success(null);
    }

    /** DELETE /articles/{id} - 删除文章 */
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.removeArticle(userId, id);
        return ApiResponse.success(null);
    }

    /** POST /articles/{id}/publish - 管理员发布文章 */
    @SaCheckRole("ADMIN")
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable("id") Long id) {
        articleService.publishArticle(id);
        return ApiResponse.success(null);
    }

    /** POST /articles/{id}/view - 记录文章浏览量 */
    @PostMapping("/{id}/view")
    public ApiResponse<Void> increaseView(@PathVariable("id") Long id) {
        articleService.increaseView(id);
        return ApiResponse.success(null);
    }

    /** POST /articles/{id}/like - 点赞文章 */
    @SaCheckLogin
    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(@PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.likeArticle(id, userId);
        return ApiResponse.success(null);
    }
}
