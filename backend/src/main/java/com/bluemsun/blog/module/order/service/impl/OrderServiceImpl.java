package com.bluemsun.blog.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.mapper.ArticleMapper;
import com.bluemsun.blog.module.order.convert.OrderConvert;
import com.bluemsun.blog.module.order.dto.OrderCreateDTO;
import com.bluemsun.blog.module.order.entity.Order;
import com.bluemsun.blog.module.order.mapper.OrderMapper;
import com.bluemsun.blog.module.order.service.OrderService;
import com.bluemsun.blog.module.order.vo.OrderPaymentVO;
import com.bluemsun.blog.module.order.vo.OrderVO;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public OrderServiceImpl(ArticleMapper articleMapper, UserMapper userMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateDTO request) {
        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        if (!"PAID".equalsIgnoreCase(article.getType())) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "该文章无需付费");
        }
        if (!"PUBLISHED".equalsIgnoreCase(article.getStatus())) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "文章尚未发布，无法购买");
        }
        if (article.getPrice() == null) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "文章未设置价格，无法购买");
        }

        LambdaQueryWrapper<Order> existsWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getArticleId, request.getArticleId())
                .eq(Order::getStatus, "PAID");
        if (count(existsWrapper) > 0) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "您已购买该文章");
        }

        Order entity = OrderConvert.toEntity(userId, request);
        entity.setAmount(article.getPrice());
        entity.setStatus("CREATED");
        save(entity);
        return OrderConvert.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo) {
        Order order = getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(ApiCode.NOT_FOUND, "订单不存在");
        }
        ensureOrderAvailable(order);
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentVO createPayment(Long userId, String orderNo, String payChannel) {
        Order order = getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(ApiCode.NOT_FOUND, "订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ApiCode.FORBIDDEN, "无法操作他人订单");
        }
        ensureOrderAvailable(order);

        if (StringUtils.hasText(payChannel)) {
            order.setPayChannel(payChannel);
        }
        order.setStatus("PAYING");
        updateById(order);

        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setAmount(order.getAmount());
        vo.setPayChannel(order.getPayChannel());
        vo.setStatus(order.getStatus());
        vo.setExpireTime(order.getExpireTime());
        vo.setPayUrl("https://pay.mock.bluemsun/order/" + order.getOrderNo());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPaySuccess(Long userId, String orderNo) {
        Order order = getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(ApiCode.NOT_FOUND, "订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ApiCode.FORBIDDEN, "无法操作他人订单");
        }
        ensureOrderAvailable(order);
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payByBalance(Long userId, String orderNo) {
        Order order = getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(ApiCode.NOT_FOUND, "订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ApiCode.FORBIDDEN, "无法操作他人订单");
        }
        ensureOrderAvailable(order);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ApiCode.NOT_FOUND, "用户不存在");
        }
        BigDecimal amount = order.getAmount();
        if (amount == null) {
            throw new BizException(ApiCode.SERVER_ERROR, "订单金额异常");
        }
        BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        if (balance.compareTo(amount) < 0) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "余额不足");
        }

        user.setBalance(balance.subtract(amount));
        userMapper.updateById(user);

        order.setStatus("PAID");
        order.setPayChannel("BALANCE");
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public IPage<OrderVO> pageByUser(Long userId, int pageNum, int pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        IPage<Order> orderPage = page(page, new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime));
        return orderPage.convert(OrderConvert::toVO);
    }

    @Override
    public IPage<OrderVO> pageAll(int pageNum, int pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        IPage<Order> orderPage = page(page, new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));
        return orderPage.convert(OrderConvert::toVO);
    }

    private void ensureOrderAvailable(Order order) {
        if ("PAID".equalsIgnoreCase(order.getStatus())) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "订单已支付");
        }
        if ("CLOSED".equalsIgnoreCase(order.getStatus())) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "订单已关闭，请重新下单");
        }
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            order.setStatus("CLOSED");
            updateById(order);
            throw new BizException(ApiCode.BUSINESS_ERROR, "订单已过期，请重新下单");
        }
    }
}
