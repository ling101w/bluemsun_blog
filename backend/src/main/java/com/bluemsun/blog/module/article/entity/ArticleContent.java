package com.bluemsun.blog.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章正文实体。
 */
@Data
@TableName("t_article_content")
public class ArticleContent {

    @TableId
    private Long id;
    private String contentMd;
    private String contentHtml;
    private String attachments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

