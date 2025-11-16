package com.bluemsun.blog.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import com.bluemsun.blog.module.user.service.UserRoleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 配置：定义权限数据源、拦截器等。
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    private final UserRoleService userRoleService;

    public SaTokenConfigure(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * 自定义权限数据，实现不同角色的权限控制。
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                if (loginId == null) {
                    return Collections.emptyList();
                }
                return userRoleService.listRoleCodesByUser(Long.parseLong(loginId.toString()));
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}

