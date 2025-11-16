package com.bluemsun.blog.module.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.mapper.ArticleMapper;
import com.bluemsun.blog.module.comment.convert.CommentConvert;
import com.bluemsun.blog.module.comment.dto.CommentCreateDTO;
import com.bluemsun.blog.module.comment.entity.Comment;
import com.bluemsun.blog.module.comment.mapper.CommentMapper;
import com.bluemsun.blog.module.comment.service.CommentNotificationService;
import com.bluemsun.blog.module.comment.service.CommentService;
import com.bluemsun.blog.module.comment.vo.CommentTreeVO;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentNotificationService notificationService;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int DAILY_LIKE_LIMIT = 10;
    private static final String COMMENT_LIKE_LIMIT_KEY_PREFIX = "like:comment:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public CommentServiceImpl(CommentMapper commentMapper,
                              CommentNotificationService notificationService,
                              ArticleMapper articleMapper,
                              UserMapper userMapper,
                              StringRedisTemplate stringRedisTemplate) {
        this.baseMapper = commentMapper;
        this.notificationService = notificationService;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Create a comment, mark it approved immediately, fill rootId, and dispatch notifications.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, CommentCreateDTO request) {
        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null) {
            throw new BizException(ApiCode.NOT_FOUND, "\u6587\u7ae0\u4e0d\u5b58\u5728");
        }
        if (Boolean.FALSE.equals(article.getAllowComment())) {
            throw new BizException(ApiCode.BAD_REQUEST, "\u8be5\u6587\u7ae0\u5df2\u5173\u95ed\u8bc4\u8bba");
        }

        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setStatus("APPROVED");
        comment.setLikeCount(0);
        save(comment);

        if (comment.getParentId() != null) {
            Comment parent = getById(comment.getParentId());
            comment.setRootId(parent != null && parent.getRootId() != null ? parent.getRootId() : comment.getParentId());
        } else {
            comment.setRootId(comment.getId());
        }
        updateById(comment);

        notificationService.sendCommentNotice(comment);
        return comment.getId();
    }

    /**
     * Like comment with Redis quota checks and atomic SQL increment.
     */
    @Override
    public void likeComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new BizException(ApiCode.NOT_FOUND, "\u8bc4\u8bba\u4e0d\u5b58\u5728");
        }
        if (!acquireLikeQuota(commentId, userId)) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "\u4eca\u65e5\u5bf9\u8be5\u8bc4\u8bba\u7684\u70b9\u8d5e\u6b21\u6570\u5df2\u8fbe\u4e0a\u9650");
        }
        update(null, Wrappers.<Comment>lambdaUpdate()
                .eq(Comment::getId, commentId)
                .set(Comment::getUpdateTime, LocalDateTime.now())
                .setSql("like_count = like_count + 1"));
    }

    /**
     * Build comment tree (approved only) for a given article.
     */
    @Override
    public List<CommentTreeVO> listTreeByArticle(Long articleId) {
        LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, "APPROVED")
                .orderByAsc(Comment::getCreateTime);
        List<Comment> comments = list(query);
        if (comments.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userIndex = CommentConvert.indexUser(userMapper.selectBatchIds(userIds));

        Map<Long, CommentTreeVO> nodeMap = new LinkedHashMap<>(comments.size());
        List<CommentTreeVO> roots = new ArrayList<>();
        for (Comment comment : comments) {
            CommentTreeVO node = CommentConvert.toTreeVO(comment, userIndex.get(comment.getUserId()));
            node.setContent(parseEmoji(node.getContent()));
            nodeMap.put(node.getId(), node);
        }
        for (Comment comment : comments) {
            CommentTreeVO current = nodeMap.get(comment.getId());
            Long parentId = comment.getParentId();
            if (parentId == null || parentId == 0 || parentId.equals(comment.getId())) {
                roots.add(current);
            } else {
                CommentTreeVO parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(current);
                } else {
                    roots.add(current);
                }
            }
        }
        return roots;
    }

    @Override
    public String previewEmoji(String content) {
        return parseEmoji(content);
    }

    private String parseEmoji(String content) {
        if (content == null) {
            return null;
        }
        String result = content;
        for (Map.Entry<String, String> entry : emojiDictionary().entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private Map<String, String> emojiDictionary() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(":smile:", "\uD83D\uDE04");
        map.put(":laughing:", "\uD83D\uDE06");
        map.put(":wink:", "\uD83D\uDE09");
        map.put(":thumbs_up:", "\uD83D\uDC4D");
        map.put(":fire:", "\uD83D\uDD25");
        map.put(":rocket:", "\uD83D\uDE80");
        map.put(":sparkles:", "\u2728");
        map.put(":idea:", "\uD83D\uDCA1");
        map.put(":heart:", "\u2764\uFE0F");
        map.put(":pray:", "\uD83D\uDE4F");
        return map;
    }

    private boolean acquireLikeQuota(Long commentId, Long userId) {
        if (userId == null) {
            return true;
        }
        String dateKey = LocalDate.now().format(DATE_FORMATTER);
        String key = COMMENT_LIKE_LIMIT_KEY_PREFIX + commentId + ':' + userId + ':' + dateKey;
        Long value = stringRedisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            stringRedisTemplate.expire(key, Duration.ofDays(1));
        }
        if (value != null && value > DAILY_LIKE_LIMIT) {
            stringRedisTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;
    }
}
