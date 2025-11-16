package com.bluemsun.blog.module.order.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 创建订单请求。
 */
public class OrderCreateDTO {

    @NotNull(message = "文章不能为空")
    private Long articleId;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String payChannel;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }
}

