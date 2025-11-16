package com.bluemsun.blog.module.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.blog.module.article.dto.ArticleCreateDTO;
import com.bluemsun.blog.module.article.dto.ArticleQueryDTO;
import com.bluemsun.blog.module.article.dto.ArticleUpdateDTO;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.vo.ArticleDetailVO;
import com.bluemsun.blog.module.article.vo.ArticleListVO;
import com.bluemsun.blog.module.article.vo.HotCreatorVO;

import java.util.List;

/**
 * 文章领域服务。
 */
public interface ArticleService extends IService<Article> {

    Long createArticle(Long authorId, ArticleCreateDTO request);

    void updateArticle(Long authorId, Long articleId, ArticleUpdateDTO request);

    void publishArticle(Long articleId);

    ArticleDetailVO getArticleDetail(Long articleId, boolean includeDraft, Long viewerId, boolean viewerIsAdmin);

    IPage<ArticleListVO> pageArticles(ArticleQueryDTO queryDTO);

    void removeArticle(Long authorId, Long articleId);

    void increaseView(Long articleId);

    void likeArticle(Long articleId, Long userId);

    void offlineArticle(Long articleId);

    void pinArticle(Long articleId);

    void unpinArticle(Long articleId);

    /**
     * 热门文章（按浏览量降序）
     */
    List<ArticleListVO> listHot(int limit);

    /**
     * 当前公告（若存在置顶文章）
     */
    ArticleDetailVO getPinnedAnnouncement();

    /**
     * 热门创作者推荐
     */
    List<HotCreatorVO> listHotCreators(int limit);
}
