package com.bluemsun.blog.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluemsun.blog.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper，交由 MyBatis-Plus 生成基础 CRUD。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

