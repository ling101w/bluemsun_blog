package com.bluemsun.blog.module.article.convert;

import com.bluemsun.blog.module.article.dto.ArticleCreateDTO;
import com.bluemsun.blog.module.article.dto.ArticleUpdateDTO;
import com.bluemsun.blog.module.article.entity.Article;
import com.bluemsun.blog.module.article.entity.ArticleContent;
import com.bluemsun.blog.module.article.entity.ArticleStat;
import com.bluemsun.blog.module.article.vo.ArticleDetailVO;
import com.bluemsun.blog.module.article.vo.ArticleListVO;

import java.time.LocalDateTime;

/**
 * 文章模型转换工具。
 */
public final class ArticleConvert {

    private ArticleConvert() {
    }

    public static Article toEntity(ArticleCreateDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setCoverUrl(dto.getCoverUrl());
        article.setCategoryId(dto.getCategoryId());
        article.setType(dto.getType());
        article.setPrice(dto.getPrice());
        boolean publish = dto.getPublish() == null || Boolean.TRUE.equals(dto.getPublish());
        article.setStatus(publish ? "PUBLISHED" : "DRAFT");
        article.setPublishTime(publish ? LocalDateTime.now() : null);
        Boolean allowComment = dto.getAllowComment();
        article.setAllowComment(allowComment == null ? Boolean.TRUE : allowComment);
        article.setPinned(Boolean.FALSE);
        article.setPinnedTime(null);
        return article;
    }

    public static void fillEntity(Article article, ArticleUpdateDTO dto) {
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setCoverUrl(dto.getCoverUrl());
        article.setCategoryId(dto.getCategoryId());
        if (dto.getType() != null) {
            article.setType(dto.getType());
        }
        if (dto.getPrice() != null) {
            article.setPrice(dto.getPrice());
        }
        if (dto.getAllowComment() != null) {
            article.setAllowComment(dto.getAllowComment());
        }
        if (dto.getPublish() != null) {
            if (Boolean.TRUE.equals(dto.getPublish())) {
                article.setStatus("PUBLISHED");
                article.setPublishTime(LocalDateTime.now());
            } else {
                article.setStatus("DRAFT");
                article.setPublishTime(null);
            }
        }
    }

    public static ArticleContent toContent(Long articleId, String markdown, String html, String attachments) {
        ArticleContent content = new ArticleContent();
        content.setId(articleId);
        content.setContentMd(markdown);
        content.setContentHtml(html);
        content.setAttachments(attachments);
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        return content;
    }

    public static ArticleListVO toListVO(Article article, ArticleStat stat) {
        ArticleListVO vo = new ArticleListVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverUrl(article.getCoverUrl());
        vo.setType(article.getType());
        vo.setPrice(article.getPrice());
        vo.setStatus(article.getStatus());
        vo.setPublishTime(article.getPublishTime());
        vo.setPinned(article.getPinned());
        vo.setPinnedTime(article.getPinnedTime());
        if (stat != null) {
            vo.setViewCount(stat.getViewCount());
            vo.setLikeCount(stat.getLikeCount());
        }
        return vo;
    }

    public static ArticleDetailVO toDetailVO(Article article, ArticleContent content, ArticleStat stat) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setAuthorId(article.getAuthorId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverUrl(article.getCoverUrl());
        vo.setCategoryId(article.getCategoryId());
        vo.setType(article.getType());
        vo.setPrice(article.getPrice());
        vo.setStatus(article.getStatus());
        vo.setAllowComment(article.getAllowComment());
        vo.setPublishTime(article.getPublishTime());
        vo.setPinned(article.getPinned());
        vo.setPinnedTime(article.getPinnedTime());
        if (content != null) {
            vo.setContentMd(content.getContentMd());
            vo.setContentHtml(content.getContentHtml());
            vo.setAttachments(content.getAttachments());
        }
        if (stat != null) {
            vo.setViewCount(stat.getViewCount());
            vo.setLikeCount(stat.getLikeCount());
            vo.setCommentCount(stat.getCommentCount());
        }
        vo.setLocked(false);
        vo.setPurchased(false);
        vo.setCanManageAttachments(false);
        vo.setOwner(false);
        return vo;
    }

    public static ArticleStat initStat(Long articleId) {
        ArticleStat stat = new ArticleStat();
        stat.setId(articleId);
        stat.setViewCount(0L);
        stat.setLikeCount(0L);
        stat.setCommentCount(0L);
        stat.setPayCount(0L);
        stat.setUpdateTime(LocalDateTime.now());
        return stat;
    }
}

