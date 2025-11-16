package com.bluemsun.blog.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.user.dto.UserUpdateProfileDTO;
import com.bluemsun.blog.module.user.service.UserService;
import com.bluemsun.blog.module.user.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心接口。
 */
@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询当前登录用户信息。
     */
    /** GET /users/me - 查询当前用户资料 */
    @SaCheckLogin
    @GetMapping("/me")
    public ApiResponse<UserProfileVO> me() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.profile(userId);
    }

    /**
     * 更新当前用户资料。
     */
    /** PUT /users/me - 更新当前用户资料 */
    @SaCheckLogin
    @PutMapping("/me")
    public ApiResponse<Boolean> updateMe(@Valid @RequestBody UserUpdateProfileDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.updateProfile(userId, request);
    }
}

