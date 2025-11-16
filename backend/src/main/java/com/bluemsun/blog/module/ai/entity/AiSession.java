package com.bluemsun.blog.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bluemsun.blog.common.core.BaseEntity;

@TableName("t_ai_session")
public class AiSession extends BaseEntity {

    private Long userId;

    private String scene;

    private String prompt;

    private String response;

    private Integer tokenUsage;

    private String status;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Integer getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(Integer tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


