package com.bluemsun.blog.module.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.blog.module.order.dto.OrderCreateDTO;
import com.bluemsun.blog.module.order.entity.Order;
import com.bluemsun.blog.module.order.vo.OrderPaymentVO;
import com.bluemsun.blog.module.order.vo.OrderVO;

/**
 * 订单服务。
 */
public interface OrderService extends IService<Order> {

    OrderVO createOrder(Long userId, OrderCreateDTO request);

    void paySuccess(String orderNo);

    OrderPaymentVO createPayment(Long userId, String orderNo, String payChannel);

    void mockPaySuccess(Long userId, String orderNo);

    void payByBalance(Long userId, String orderNo);

    IPage<OrderVO> pageByUser(Long userId, int pageNum, int pageSize);

    IPage<OrderVO> pageAll(int pageNum, int pageSize);
}

