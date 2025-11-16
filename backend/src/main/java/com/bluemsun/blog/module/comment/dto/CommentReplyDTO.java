package com.bluemsun.blog.module.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 回复评论请求。
 */
public class CommentReplyDTO {

    @NotNull(message = "文章 ID 不能为空")
    private Long articleId;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容过长")
    private String content;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}


