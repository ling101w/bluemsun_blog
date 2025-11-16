package com.bluemsun.blog.module.user.convert;

import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.vo.UserProfileVO;

/**
 * 用户模型转换工具，暂时手写，后续可接入 MapStruct。
 */
public final class UserConvert {

    private UserConvert() {
    }

    public static UserProfileVO toProfile(User user) {
        if (user == null) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setProfile(user.getProfile());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setBalance(user.getBalance());
        return vo;
    }
}

