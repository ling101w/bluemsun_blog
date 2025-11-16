package com.bluemsun.blog.module.order.convert;

import com.bluemsun.blog.module.order.dto.OrderCreateDTO;
import com.bluemsun.blog.module.order.entity.Order;
import com.bluemsun.blog.module.order.vo.OrderVO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单转换工具。
 */
public final class OrderConvert {

    private OrderConvert() {
    }

    public static Order toEntity(Long userId, OrderCreateDTO request) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setArticleId(request.getArticleId());
        order.setAmount(request.getAmount());
        order.setPayChannel(request.getPayChannel());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return order;
    }

    public static OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setArticleId(order.getArticleId());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setPayChannel(order.getPayChannel());
        vo.setPayTime(order.getPayTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setExpireTime(order.getExpireTime());
        return vo;
    }

    private static String generateOrderNo() {
        return "BMS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

