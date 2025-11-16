package com.bluemsun.blog.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bluemsun.blog.common.core.BaseEntity;

/**
 * 用户角色关联。
 */
@TableName("t_user_role")
public class UserRole extends BaseEntity {

    private Long userId;

    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}

