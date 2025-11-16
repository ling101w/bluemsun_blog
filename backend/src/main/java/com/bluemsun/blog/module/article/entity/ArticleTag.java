package com.bluemsun.blog.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bluemsun.blog.common.core.BaseEntity;

/**
 * 文章-标签关联。
 */
@TableName("rel_article_tag")
public class ArticleTag extends BaseEntity {

    private Long articleId;
    private Long tagId;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}

