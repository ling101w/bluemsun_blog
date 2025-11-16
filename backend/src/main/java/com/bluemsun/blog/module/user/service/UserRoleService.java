package com.bluemsun.blog.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.blog.module.user.entity.UserRole;

import java.util.List;

public interface UserRoleService extends IService<UserRole> {

    List<String> listRoleCodesByUser(Long userId);
}
