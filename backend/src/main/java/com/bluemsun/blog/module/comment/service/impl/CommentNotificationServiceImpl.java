package com.bluemsun.blog.module.comment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.mapper.ArticleMapper;
import com.bluemsun.blog.module.comment.entity.Comment;
import com.bluemsun.blog.module.comment.mapper.CommentMapper;
import com.bluemsun.blog.module.comment.service.CommentNotificationService;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class CommentNotificationServiceImpl implements CommentNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CommentNotificationServiceImpl.class);

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    @Value("${spring.mail.username:}")
    private String from;

    public CommentNotificationServiceImpl(JavaMailSender mailSender,
                                          UserMapper userMapper,
                                          ArticleMapper articleMapper,
                                          CommentMapper commentMapper) {
        this.mailSender = mailSender;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    @Async
    public void sendCommentNotice(Comment comment) {
        if (comment == null || comment.getUserId() == null) {
            return;
        }
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article == null) {
            return;
        }
        Comment parent = comment.getParentId() != null ? commentMapper.selectById(comment.getParentId()) : null;
        Long targetUserId = parent != null ? parent.getUserId() : article.getAuthorId();
        if (Objects.equals(targetUserId, comment.getUserId())) {
            return;
        }
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null || !StringUtils.hasText(targetUser.getEmail())) {
            return;
        }
        sendMail(targetUser.getEmail(), article.getTitle(), comment.getContent());
    }

    private void sendMail(String to, String articleTitle, String content) {
        if (!StringUtils.hasText(from)) {
            log.warn("发件地址未配置，无法发送评论通知");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("【Bluemsun 博客】收到新的评论通知");
            message.setText("文章：《" + articleTitle + "》\n评论内容：" + content);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("发送评论通知失败", e);
        }
    }
}


