package com.bluemsun.blog.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.user.dto.UserLoginDTO;
import com.bluemsun.blog.module.user.dto.UserRegisterDTO;
import com.bluemsun.blog.module.user.dto.UserUpdateProfileDTO;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.vo.UserProfileVO;

/**
 * 用户领域服务接口，封装主要业务场景。
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册。
     */
    ApiResponse<Boolean> register(UserRegisterDTO request);

    /**
     * 用户登录，返回 token 或登录状态。
     */
    ApiResponse<String> login(UserLoginDTO request);

    /**
     * 查询个人资料。
     */
    ApiResponse<UserProfileVO> profile(Long userId);

    /**
     * 更新个人资料。
     */
    ApiResponse<Boolean> updateProfile(Long userId, UserUpdateProfileDTO request);
}

