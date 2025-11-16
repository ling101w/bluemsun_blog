package com.bluemsun.blog.module.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.module.article.convert.ArticleConvert;
import com.bluemsun.blog.module.article.dto.ArticleCreateDTO;
import com.bluemsun.blog.module.article.dto.ArticleQueryDTO;
import com.bluemsun.blog.module.article.dto.ArticleUpdateDTO;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.entity.ArticleContent;
import com.bluemsun.blog.module.article.entity.ArticleStat;
import com.bluemsun.blog.module.article.mapper.ArticleContentMapper;
import com.bluemsun.blog.module.article.mapper.ArticleMapper;
import com.bluemsun.blog.module.article.mapper.ArticleStatMapper;
import com.bluemsun.blog.module.article.service.ArticleService;
import com.bluemsun.blog.module.article.service.AttachmentService;
import com.bluemsun.blog.module.article.vo.ArticleDetailVO;
import com.bluemsun.blog.module.article.vo.ArticleListVO;
import com.bluemsun.blog.module.article.vo.HotCreatorVO;
import com.bluemsun.blog.module.order.entity.Order;
import com.bluemsun.blog.module.order.mapper.OrderMapper;
import com.bluemsun.blog.module.user.entity.User;
import com.bluemsun.blog.module.user.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleContentMapper articleContentMapper;
    private final ArticleStatMapper articleStatMapper;
    private final AttachmentService attachmentService;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int DAILY_LIKE_LIMIT = 10;
    private static final String ARTICLE_LIKE_LIMIT_KEY_PREFIX = "like:article:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public ArticleServiceImpl(ArticleContentMapper articleContentMapper,
                              ArticleStatMapper articleStatMapper,
                              AttachmentService attachmentService,
                              OrderMapper orderMapper,
                              UserMapper userMapper,
                              StringRedisTemplate stringRedisTemplate) {
        this.articleContentMapper = articleContentMapper;
        this.articleStatMapper = articleStatMapper;
        this.attachmentService = attachmentService;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 新建文章：根据传入 publish 标识决定保存为草稿或立即发布，并初始化正文/统计信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(Long authorId, ArticleCreateDTO request) {
        Article entity = ArticleConvert.toEntity(request);
        entity.setAuthorId(authorId);
        save(entity);

        ArticleContent content = ArticleConvert.toContent(entity.getId(), request.getContentMd(), request.getContentHtml(), request.getAttachments());
        articleContentMapper.insert(content);

        ArticleStat stat = ArticleConvert.initStat(entity.getId());
        articleStatMapper.insert(stat);

        return entity.getId();
    }

    /**
     * 编辑文章：只允许作者本人操作，支持在草稿与发布之间切换。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long authorId, Long articleId, ArticleUpdateDTO request) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted()) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在或已删除");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BizException(ApiCode.FORBIDDEN, "只能编辑自己的文章");
        }

        ArticleConvert.fillEntity(article, request);
        updateById(article);

        ArticleContent content = articleContentMapper.selectById(articleId);
        if (content == null) {
            content = ArticleConvert.toContent(articleId, request.getContentMd(), request.getContentHtml(), request.getAttachments());
            articleContentMapper.insert(content);
        } else {
            content.setContentMd(request.getContentMd());
            content.setContentHtml(request.getContentHtml());
            content.setAttachments(request.getAttachments());
            content.setUpdateTime(LocalDateTime.now());
            articleContentMapper.updateById(content);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long articleId) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted()) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        article.setStatus("PUBLISHED");
        article.setPublishTime(LocalDateTime.now());
        updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineArticle(Long articleId) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted()) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        article.setStatus("OFFLINE");
        article.setPinned(Boolean.FALSE);
        article.setPinnedTime(null);
        updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinArticle(Long articleId) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted()) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        if (!"PUBLISHED".equalsIgnoreCase(article.getStatus())) {
            throw new BizException(ApiCode.BAD_REQUEST, "仅支持已发布文章设为公告");
        }
        if ("PAID".equalsIgnoreCase(article.getType())) {
            throw new BizException(ApiCode.BAD_REQUEST, "付费文章不可设为公告");
        }
        update(Wrappers.<Article>lambdaUpdate()
                .eq(Article::getPinned, true)
                .set(Article::getPinned, false)
                .set(Article::getPinnedTime, null));
        article.setPinned(Boolean.TRUE);
        article.setPinnedTime(LocalDateTime.now());
        updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpinArticle(Long articleId) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted()) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        article.setPinned(Boolean.FALSE);
        article.setPinnedTime(null);
        updateById(article);
    }

    @Override
    public ArticleDetailVO getPinnedAnnouncement() {
        Article article = getOne(Wrappers.<Article>lambdaQuery()
                .eq(Article::getPinned, true)
                .eq(Article::getStatus, "PUBLISHED")
                .orderByDesc(Article::getPinnedTime)
                .last("LIMIT 1"), false);
        if (article == null) {
            return null;
        }
        ArticleContent content = articleContentMapper.selectById(article.getId());
        ArticleStat stat = articleStatMapper.selectById(article.getId());
        return ArticleConvert.toDetailVO(article, content, stat);
    }

    /**
     * 查询详情：根据查看者身份决定是否允许读取草稿或付费内容，并动态设置锁定标识。
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long articleId, boolean includeDraft, Long viewerId, boolean viewerIsAdmin) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted()) {
            throw new BizException(ApiCode.NOT_FOUND, "文章不存在");
        }
        if (!includeDraft && "PUBLISHED".equals(article.getStatus()) == false) {
            throw new BizException(ApiCode.FORBIDDEN, "文章未发布");
        }
        ArticleContent content = articleContentMapper.selectById(articleId);
        ArticleStat stat = articleStatMapper.selectById(articleId);
        ArticleDetailVO detailVO = ArticleConvert.toDetailVO(article, content, stat);

        boolean isOwner = viewerId != null && viewerId.equals(article.getAuthorId());
        boolean isManager = isOwner || viewerIsAdmin;

        boolean purchased = !"PAID".equalsIgnoreCase(article.getType()) || isOwner;

        if ("PAID".equalsIgnoreCase(article.getType()) && !isOwner) {
            if (viewerId != null) {
                purchased = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, viewerId)
                        .eq(Order::getArticleId, article.getId())
                        .eq(Order::getStatus, "PAID")) > 0;
            }
            if (!purchased && !viewerIsAdmin) {
                detailVO.setContentMd(null);
                detailVO.setContentHtml(null);
                detailVO.setAttachments(null);
                detailVO.setLocked(true);
            }
        }

        detailVO.setPurchased(purchased);
        detailVO.setOwner(isOwner);
        detailVO.setCanManageAttachments(isManager);
        return detailVO;
    }

    /**
     * 分页查询：批量加载统计数据，在首页场景下也能保持稳定性能。
     */
    @Override
    public IPage<ArticleListVO> pageArticles(ArticleQueryDTO queryDTO) {
        Page<Article> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(StringUtils.hasText(queryDTO.getStatus()), Article::getStatus, queryDTO.getStatus())
                .eq(StringUtils.hasText(queryDTO.getType()), Article::getType, queryDTO.getType())
                .eq(queryDTO.getCategoryId() != null, Article::getCategoryId, queryDTO.getCategoryId())
                .like(StringUtils.hasText(queryDTO.getKeyword()), Article::getTitle, queryDTO.getKeyword())
                .orderByDesc(Article::getPinned)
                .orderByDesc(Article::getPinnedTime)
                .orderByDesc(Article::getPublishTime, Article::getCreateTime);
        IPage<Article> articlePage = page(page, wrapper);

        Map<Long, ArticleStat> statIndex = Map.of();
        List<Article> records = articlePage.getRecords();
        if (!records.isEmpty()) {
            List<Long> ids = records.stream().map(Article::getId).toList();
            List<ArticleStat> stats = articleStatMapper.selectBatchIds(ids);
            statIndex = stats.stream().collect(Collectors.toMap(ArticleStat::getId, it -> it));
        }

        Page<ArticleListVO> resultPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        Map<Long, ArticleStat> finalStatIndex = statIndex;
        resultPage.setRecords(records.stream()
                .map(article -> ArticleConvert.toListVO(article, finalStatIndex.get(article.getId())))
                .toList());
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArticle(Long authorId, Long articleId) {
        Article article = getById(articleId);
        if (article == null) {
            return;
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new BizException(ApiCode.FORBIDDEN, "只能删除自己的文章");
        }
        attachmentService.removeByArticle(articleId);
        removeById(articleId);
        articleContentMapper.deleteById(articleId);
        articleStatMapper.deleteById(articleId);
    }

    /**
     * 阅读计数：使用 SQL 原子自增，避免高并发下的覆盖问题。
     */
    @Override
    public void increaseView(Long articleId) {
        articleStatMapper.update(null, Wrappers.<ArticleStat>lambdaUpdate()
                .eq(ArticleStat::getId, articleId)
                .set(ArticleStat::getUpdateTime, LocalDateTime.now())
                .setSql("view_count = view_count + 1"));
    }

    /**
     * 点赞计数：同样采用 SQL 自增，保证计数准确。
     */
    @Override
    public void likeArticle(Long articleId, Long userId) {
        if (!acquireDailyLikeQuota(ARTICLE_LIKE_LIMIT_KEY_PREFIX, articleId, userId)) {
            throw new BizException(ApiCode.BUSINESS_ERROR, "今日对该文章点赞次数已达上限");
        }
        articleStatMapper.update(null, Wrappers.<ArticleStat>lambdaUpdate()
                .eq(ArticleStat::getId, articleId)
                .set(ArticleStat::getUpdateTime, LocalDateTime.now())
                .setSql("like_count = like_count + 1"));
    }

    /**
     * 热门文章：一次性取出统计与文章实体，再过滤未发布/已删记录。
     */
    @Override
    public List<ArticleListVO> listHot(int limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        List<ArticleStat> topStats = articleStatMapper.selectList(Wrappers.<ArticleStat>lambdaQuery()
                .orderByDesc(ArticleStat::getViewCount)
                .last("LIMIT " + size));
        if (topStats == null || topStats.isEmpty()) {
            return List.of();
        }
        List<Long> ids = topStats.stream().map(ArticleStat::getId).toList();
        Map<Long, Article> articleIndex = listByIds(ids).stream()
                .collect(Collectors.toMap(Article::getId, it -> it));

        List<ArticleListVO> result = new ArrayList<>(topStats.size());
        for (ArticleStat stat : topStats) {
            Article article = articleIndex.get(stat.getId());
            if (article == null || article.getDeleted() || !"PUBLISHED".equalsIgnoreCase(article.getStatus())) {
                continue;
            }
            result.add(ArticleConvert.toListVO(article, stat));
        }
        return result;
    }

    @Override
    public List<HotCreatorVO> listHotCreators(int limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        int fetch = size * 5;
        List<ArticleStat> topStats = articleStatMapper.selectList(Wrappers.<ArticleStat>lambdaQuery()
                .orderByDesc(ArticleStat::getViewCount)
                .last("LIMIT " + fetch));
        if (topStats.isEmpty()) {
            return List.of();
        }
        List<Long> articleIds = topStats.stream().map(ArticleStat::getId).toList();
        Map<Long, Article> articleIndex = listByIds(articleIds).stream()
                .filter(article -> article != null && !Boolean.TRUE.equals(article.getDeleted()))
                .filter(article -> "PUBLISHED".equalsIgnoreCase(article.getStatus()))
                .collect(Collectors.toMap(Article::getId, it -> it, (a, b) -> a));
        Map<Long, CreatorAggregate> aggregate = new HashMap<>();
        for (ArticleStat stat : topStats) {
            Article article = articleIndex.get(stat.getId());
            if (article == null) {
                continue;
            }
            CreatorAggregate agg = aggregate.computeIfAbsent(article.getAuthorId(), id -> new CreatorAggregate(id));
            agg.articleCount++;
            agg.viewCount += stat.getViewCount() == null ? 0 : stat.getViewCount();
            agg.likeCount += stat.getLikeCount() == null ? 0 : stat.getLikeCount();
        }
        if (aggregate.isEmpty()) {
            return List.of();
        }
        List<Map.Entry<Long, CreatorAggregate>> ranked = aggregate.entrySet().stream()
                .sorted((a, b) -> {
                    CreatorAggregate left = a.getValue();
                    CreatorAggregate right = b.getValue();
                    int cmp = Long.compare(right.viewCount, left.viewCount);
                    if (cmp != 0) {
                        return cmp;
                    }
                    cmp = Long.compare(right.likeCount, left.likeCount);
                    if (cmp != 0) {
                        return cmp;
                    }
                    return Integer.compare(right.articleCount, left.articleCount);
                })
                .limit(size)
                .toList();
        List<Long> authorIds = ranked.stream().map(Map.Entry::getKey).toList();
        Map<Long, User> userIndex = userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, it -> it));
        List<HotCreatorVO> result = new ArrayList<>(ranked.size());
        for (Map.Entry<Long, CreatorAggregate> entry : ranked) {
            CreatorAggregate agg = entry.getValue();
            User user = userIndex.get(entry.getKey());
            HotCreatorVO vo = new HotCreatorVO();
            vo.setId(entry.getKey());
            vo.setNickname(user != null && StringUtils.hasText(user.getNickname()) ? user.getNickname() : ("用户" + entry.getKey()));
            vo.setAvatar(user != null ? user.getAvatar() : null);
            vo.setArticleCount(agg.articleCount);
            vo.setViewCount(agg.viewCount);
            vo.setLikeCount(agg.likeCount);
            result.add(vo);
        }
        return result;
    }

    private boolean acquireDailyLikeQuota(String prefix, Long targetId, Long userId) {
        if (userId == null) {
            return true;
        }
        String dateKey = LocalDate.now().format(DATE_FORMATTER);
        String key = prefix + targetId + ":" + userId + ":" + dateKey;
        Long counter = stringRedisTemplate.opsForValue().increment(key);
        if (counter != null && counter == 1L) {
            stringRedisTemplate.expire(key, Duration.ofDays(1));
        }
        if (counter != null && counter > DAILY_LIKE_LIMIT) {
            stringRedisTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;
    }

    private static class CreatorAggregate {
        private final long authorId;
        private int articleCount;
        private long viewCount;
        private long likeCount;

        private CreatorAggregate(long authorId) {
            this.authorId = authorId;
        }
    }
}
