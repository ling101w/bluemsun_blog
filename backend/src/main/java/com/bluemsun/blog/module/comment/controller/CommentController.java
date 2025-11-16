package com.bluemsun.blog.module.comment.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.comment.dto.CommentCreateDTO;
import com.bluemsun.blog.module.comment.dto.CommentReplyDTO;
import com.bluemsun.blog.module.comment.dto.CommentEmojiDTO;
import com.bluemsun.blog.module.comment.service.CommentService;
import com.bluemsun.blog.module.comment.vo.CommentTreeVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 前台评论列表（仅返回状态为 APPROVED 的评论，树形结构）。
     */
    /** GET /comments - 获取已审核评论的树形结构 */
    @GetMapping
    public ApiResponse<List<CommentTreeVO>> list(@RequestParam("articleId") Long articleId) {
        return ApiResponse.success(commentService.listTreeByArticle(articleId));
    }

    /**
     * 新增评论（要求登录）。
     */
    /** POST /comments - 新增评论 */
    @SaCheckLogin
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CommentCreateDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(commentService.createComment(userId, request));
    }

    /**
     * 回复某条评论（要求登录）。
     */
    /** POST /comments/{id}/reply - 回复指定评论 */
    @SaCheckLogin
    @PostMapping("/{id}/reply")
    public ApiResponse<Long> reply(@PathVariable("id") Long parentId, @Valid @RequestBody CommentReplyDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId(request.getArticleId());
        dto.setParentId(parentId);
        dto.setContent(request.getContent());
        return ApiResponse.success(commentService.createComment(userId, dto));
    }

    /**
     * 点赞评论（要求登录）。
     */
    /** POST /comments/{id}/like - 点赞评论 */
    @SaCheckLogin
    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(@PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        commentService.likeComment(id, userId);
        return ApiResponse.success(null);
    }
    //暂留
    /** PUT /comments/emoji/preview - 预览评论中的表情渲染效果 */
    @SaCheckLogin
    @PutMapping("/emoji/preview")
    public ApiResponse<String> previewEmoji(@Valid @RequestBody CommentEmojiDTO request) {
        return ApiResponse.success(commentService.previewEmoji(request.getContent()));
    }
}

