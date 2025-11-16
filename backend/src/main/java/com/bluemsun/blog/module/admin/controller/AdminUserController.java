package com.bluemsun.blog.module.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.admin.vo.AdminUserVO;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.service.UserService;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/admin/users")
@SaCheckRole("ADMIN")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /admin/users - 管理端分页查询用户 */
    @GetMapping
    public ApiResponse<IPage<AdminUserVO>> page(@RequestParam(defaultValue = "1") @Min(1) int pageNo,
                                                @RequestParam(defaultValue = "10") @Min(1) int pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status) {
        Page<User> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            query.and(w -> w.like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (StringUtils.hasText(status)) {
            query.eq(User::getStatus, status);
        }
        query.orderByDesc(User::getCreateTime);

        IPage<User> result = userService.page(page, query);
        Page<AdminUserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<AdminUserVO> records = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return ApiResponse.success(voPage);
    }

    /** POST /admin/users/{id}/status - 更新用户状态 */
    @PostMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id,
                                             @RequestParam String status) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.fail(ApiCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return ApiResponse.success(true, "状态更新成功");
    }

    /** POST /admin/users/{id}/recharge - 充值用户余额 */
    @PostMapping("/{id}/recharge")
    public ApiResponse<Boolean> recharge(@PathVariable Long id,
                                         @RequestParam("amount") BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponse.fail(ApiCode.BAD_REQUEST, "充值金额需大于 0");
        }
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.fail(ApiCode.NOT_FOUND, "用户不存在");
        }
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        user.setBalance(current.add(amount));
        userService.updateById(user);
        return ApiResponse.success(true, "充值成功");
    }

    private AdminUserVO toVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setBalance(user.getBalance());
        return vo;
    }
}
