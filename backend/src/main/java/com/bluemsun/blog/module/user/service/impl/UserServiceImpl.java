package com.bluemsun.blog.module.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.user.convert.UserConvert;
import com.bluemsun.blog.module.user.dto.UserLoginDTO;
import com.bluemsun.blog.module.user.dto.UserRegisterDTO;
import com.bluemsun.blog.module.user.dto.UserUpdateProfileDTO;
import com.bluemsun.blog.module.user.entity.LoginLog;
import com.bluemsun.blog.module.user.entity.Role;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.entity.UserRole;
import com.bluemsun.blog.module.user.mapper.LoginLogMapper;
import com.bluemsun.blog.module.user.mapper.RoleMapper;
import com.bluemsun.blog.module.user.mapper.UserMapper;
import com.bluemsun.blog.module.user.service.UserRoleService;
import com.bluemsun.blog.module.user.service.VerificationCodeService;
import com.bluemsun.blog.module.user.service.UserService;
import com.bluemsun.blog.module.user.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户服务实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;
    private final RoleMapper roleMapper;
    private final LoginLogMapper loginLogMapper;
    private final VerificationCodeService verificationCodeService;

    private static final String SCENE_REGISTER = "REGISTER";

    public UserServiceImpl(PasswordEncoder passwordEncoder,
                           UserRoleService userRoleService,
                           RoleMapper roleMapper,
                           LoginLogMapper loginLogMapper,
                           VerificationCodeService verificationCodeService) {
        this.passwordEncoder = passwordEncoder;
        this.userRoleService = userRoleService;
        this.roleMapper = roleMapper;
        this.loginLogMapper = loginLogMapper;
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Boolean> register(UserRegisterDTO request) {
        if (!verificationCodeService.validate(request.getEmail(), SCENE_REGISTER, request.getCode())) {
            throw new BizException(ApiCode.BAD_REQUEST, "验证码错误或已过期");
        }
        // 邮箱是否已存在
        boolean exists = lambdaQuery().eq(User::getEmail, request.getEmail()).exists();
        if (exists) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "邮箱已被注册，请直接登录");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus("active");
        user.setAvatar("https://img.example.com/default-avatar.png");
        user.setProfile("这家伙很神秘，还没有留下简介~");
        user.setBalance(BigDecimal.ZERO);

        boolean saved = save(user);

        Role userRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "USER"));
        if (userRole != null) {
            UserRole relation = new UserRole();
            relation.setUserId(user.getId());
            relation.setRoleId(userRole.getId());
            userRoleService.save(relation);
        }

        return ApiResponse.success(saved);
    }

    @Override
    public ApiResponse<String> login(UserLoginDTO request) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail());
        User user = getOne(query, false);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginLog(null, false, "账号或密码错误");
            throw new BizException(ApiCode.BAD_REQUEST, "账号或密码错误");
        }

        if (!Objects.equals("active", user.getStatus())) {
            throw new BizException(ApiCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        StpUtil.login(user.getId());
        String tokenValue = StpUtil.getTokenValue();
        recordLoginLog(user.getId(), true, "登录成功");
        return ApiResponse.success(tokenValue, "登录成功");
    }

    @Override
    public ApiResponse<UserProfileVO> profile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ApiCode.NOT_FOUND, "用户不存在");
        }
        return ApiResponse.success(UserConvert.toProfile(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Boolean> updateProfile(Long userId, UserUpdateProfileDTO request) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ApiCode.NOT_FOUND, "用户不存在");
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getProfile() != null) {
            user.setProfile(request.getProfile());
        }

        boolean updated = updateById(user);
        return ApiResponse.success(updated, "资料更新成功");
    }

    private void recordLoginLog(Long userId, boolean success, String message) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        String clientIp = request != null ? extractClientIp(request) : "UNKNOWN";
        String userAgent = request != null ? request.getHeader("User-Agent") : "";

        if (userId != null) {
            LoginLog log = new LoginLog();
            log.setUserId(userId);
            log.setClientIp(clientIp);
            log.setUserAgent(userAgent);
            log.setSuccess(success);
            log.setMessage(message);
            log.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(log);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}

