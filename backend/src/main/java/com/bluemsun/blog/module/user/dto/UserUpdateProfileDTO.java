package com.bluemsun.blog.module.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新个人资料请求体。
 */
public class UserUpdateProfileDTO {

    @Size(max = 32, message = "昵称长度不能超过 32 字符")
    private String nickname;

    @Size(max = 255, message = "头像地址需合理")
    private String avatar;

    @Size(max = 512, message = "个人简介过长")
    private String profile;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}

