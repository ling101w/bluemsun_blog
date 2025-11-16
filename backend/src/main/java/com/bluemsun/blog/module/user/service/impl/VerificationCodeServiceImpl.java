package com.bluemsun.blog.module.user.service.impl;

import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.user.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 邮件验证码业务实现。
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration SEND_INTERVAL = Duration.ofMinutes(1);

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender mailSender;

    private static final String CACHE_KEY_PATTERN = "auth:code:%s:%s";
    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Autowired
    public VerificationCodeServiceImpl(StringRedisTemplate stringRedisTemplate, @Autowired(required = false) JavaMailSender mailSender) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.mailSender = mailSender;
    }

    @Override
    public void sendCode(String email, String scene) {
        String normalizedScene = normalizeScene(scene);
        String redisKey = buildKey(normalizedScene, email);

        Long expire = stringRedisTemplate.getExpire(redisKey + ":interval");
        if (expire != null && expire > 0) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "发送过于频繁，请稍后重试");
        }

        String code = generateCode();
        stringRedisTemplate.opsForValue().set(redisKey, code, CODE_TTL);
        // 限频标记
        stringRedisTemplate.opsForValue().set(redisKey + ":interval", "1", SEND_INTERVAL);

        sendMail(email, normalizedScene, code);
        log.info("[验证码] 场景={} 邮箱={} code={} (调试日志)", normalizedScene, email, code);
    }

    @Override
    public boolean validate(String email, String scene, String code) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        String redisKey = buildKey(normalizeScene(scene), email);
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(cached) || !cached.equalsIgnoreCase(code.trim())) {
            return false;
        }
        stringRedisTemplate.delete(redisKey);
        return true;
    }

    private String buildKey(String scene, String email) {
        return String.format(CACHE_KEY_PATTERN, scene.toLowerCase(Locale.ROOT), email.toLowerCase(Locale.ROOT));
    }

    private String normalizeScene(String scene) {
        String normalized = scene == null ? "" : scene.trim().toUpperCase(Locale.ROOT);
        if (!"REGISTER".equals(normalized) && !"LOGIN".equals(normalized)) {
            throw new BizException(ApiCode.BAD_REQUEST, "验证码场景不支持");
        }
        return normalized;
    }

    private String generateCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return Integer.toString(value);
    }

    private void sendMail(String email, String scene, String code) {
        if (mailSender instanceof JavaMailSenderImpl) {
            JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
            if (!StringUtils.hasText(sender.getHost())) {
                log.warn("未配置邮件服务器，已使用日志输出验证码");
                return;
            }
        }

        if (mailSender == null) {
            log.warn("未注入 JavaMailSender，将以日志方式输出验证码");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // QQ/163 等要求 From 必须与认证用户名完全一致，否则 501/550
            if (StringUtils.hasText(mailUsername)) {
                message.setFrom(mailUsername);
            }
            message.setTo(email);
            message.setSubject(String.format("蓝旭博客maming %s 验证码", scene.equals("REGISTER") ? "注册" : "登录"));
            message.setText(String.format("您的验证码为 %s，10 分钟内有效。", code));
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("发送邮件失败，降级为日志输出: {}", ex.getMessage());
        }
    }
}


