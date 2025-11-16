package com.bluemsun.blog.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AiRequest {

    @NotBlank(message = "请选择任务类型")
    @Pattern(regexp = "POLISH|EXTEND|SUMMARY|TRANSLATE|CONTINUE", message = "不支持的 AI 场景")
    private String scene;

    @NotBlank(message = "请输入内容")
    @Size(max = 2000, message = "输入内容请控制在 2000 字以内")
    private String content;

    private String extra;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}


