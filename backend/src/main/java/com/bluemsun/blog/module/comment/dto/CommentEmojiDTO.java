package com.bluemsun.blog.module.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentEmojiDTO {

    @NotBlank(message = "请输入原始文本")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}


