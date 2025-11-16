package com.bluemsun.blog.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.blog.module.user.entity.Role;
import com.bluemsun.blog.module.user.entity.UserRole;
import com.bluemsun.blog.module.user.mapper.RoleMapper;
import com.bluemsun.blog.module.user.mapper.UserRoleMapper;
import com.bluemsun.blog.module.user.service.UserRoleService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    private final RoleMapper roleMapper;

    public UserRoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<String> listRoleCodesByUser(Long userId) {
        List<UserRole> relations = list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = relations.stream().map(UserRole::getRoleId).toList();
        return roleMapper.selectBatchIds(roleIds).stream().map(Role::getCode).toList();
    }
}
