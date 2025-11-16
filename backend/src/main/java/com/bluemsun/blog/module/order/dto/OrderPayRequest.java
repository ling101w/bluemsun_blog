package com.bluemsun.blog.module.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 支付发起请求。
 */
public class OrderPayRequest {

    @NotBlank(message = "请选择支付渠道")
    private String payChannel;

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }
}


