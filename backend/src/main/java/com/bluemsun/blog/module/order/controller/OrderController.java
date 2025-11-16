package com.bluemsun.blog.module.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.order.dto.OrderCreateDTO;
import com.bluemsun.blog.module.order.dto.OrderPayRequest;
import com.bluemsun.blog.module.order.service.OrderService;
import com.bluemsun.blog.module.order.vo.OrderPaymentVO;
import com.bluemsun.blog.module.order.vo.OrderVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** POST /orders - 创建订单 */
    @SaCheckLogin
    @PostMapping
    public ApiResponse<OrderVO> create(@Valid @RequestBody OrderCreateDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    /** POST /orders/{orderNo}/pay-success - 管理员标记订单支付成功 */
    @SaCheckRole("ADMIN")
    @PostMapping("/{orderNo}/pay-success")
    public ApiResponse<Void> paySuccess(@PathVariable("orderNo") String orderNo) {
        orderService.paySuccess(orderNo);
        return ApiResponse.success(null);
    }

    /** POST /orders/{orderNo}/pay - 发起支付 */
    @SaCheckLogin
    @PostMapping("/{orderNo}/pay")
    public ApiResponse<OrderPaymentVO> createPayment(@PathVariable("orderNo") String orderNo,
                                                     @Valid @RequestBody OrderPayRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(orderService.createPayment(userId, orderNo, request.getPayChannel()));
    }

    /** POST /orders/{orderNo}/pay/balance - 使用余额支付 */
    @SaCheckLogin
    @PostMapping("/{orderNo}/pay/balance")
    public ApiResponse<Void> payByBalance(@PathVariable("orderNo") String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.payByBalance(userId, orderNo);
        return ApiResponse.success(null, "余额支付成功");
    }

    /** POST /orders/{orderNo}/mock-pay - 模拟支付（测试用） */
    @SaCheckLogin
    @PostMapping("/{orderNo}/mock-pay")
    public ApiResponse<Void> mockPay(@PathVariable("orderNo") String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.mockPaySuccess(userId, orderNo);
        return ApiResponse.success(null);
    }

    /** GET /orders/my - 分页查看当前用户订单 */
    @SaCheckLogin
    @GetMapping("/my")
    public ApiResponse<IPage<OrderVO>> myOrders(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
                                                @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(orderService.pageByUser(userId, pageNum, pageSize));
    }

    /** GET /orders - 管理员分页查看全部订单 */
    @SaCheckRole("ADMIN")
    @GetMapping
    public ApiResponse<IPage<OrderVO>> allOrders(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
                                                 @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        return ApiResponse.success(orderService.pageAll(pageNum, pageSize));
    }
}
