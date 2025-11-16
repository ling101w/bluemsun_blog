package com.bluemsun.blog.module.user.service;

/**
 * 邮件验证码业务接口。
 */
public interface VerificationCodeService {

    /**
     * 发送验证码。
     *
     * @param email 邮箱
     * @param scene 场景（REGISTER / LOGIN）
     */
    void sendCode(String email, String scene);

    /**
     * 校验验证码。
     *
     * @param email 邮箱
     * @param scene 场景
     * @param code  用户输入的验证码
     * @return true 表示校验通过
     */
    boolean validate(String email, String scene, String code);
}


