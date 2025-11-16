package com.bluemsun.blog.module.user.controller;

import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.user.dto.EmailCodeRequest;
import com.bluemsun.blog.module.user.dto.UserLoginDTO;
import com.bluemsun.blog.module.user.dto.UserRegisterDTO;
import com.bluemsun.blog.module.user.service.UserService;
import com.bluemsun.blog.module.user.service.VerificationCodeService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录注册相关接口。
 */
@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    public AuthController(UserService userService, VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
    }

    /** POST /auth/code - 发送验证码 */
    @PostMapping("/code")
    public ApiResponse<Boolean> sendCode(@Valid @RequestBody EmailCodeRequest request) {
        verificationCodeService.sendCode(request.getEmail(), request.getScene());
        return ApiResponse.success(true, "验证码已发送");
    }

    /**
     * 注册新用户。
     */
    /** POST /auth/register - 注册新用户 */
    @PostMapping("/register")
    public ApiResponse<Boolean> register(@Valid @RequestBody UserRegisterDTO request) {
        return userService.register(request);
    }

    /**
     * 用户登录。
     */
    /** POST /auth/login - 用户登录 */
    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody UserLoginDTO request) {
        return userService.login(request);
    }
}

