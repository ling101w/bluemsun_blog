package com.bluemsun.blog.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章统计实体。
 */
@Data
@TableName("t_article_stat")
public class ArticleStat {

    @TableId
    private Long id;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long payCount;
    private LocalDateTime updateTime;
}

