package com.bluemsun.blog.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluemsun.blog.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文章主表实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_article")
public class Article extends BaseEntity {

    private Long authorId;
    private String title;
    private String summary;
    private String coverUrl;
    private Long categoryId;
    private String type;
    private BigDecimal price;
    private String status;
    private LocalDateTime publishTime;

    @TableField("allow_comment")
    private Boolean allowComment;
    private Boolean pinned;
    private LocalDateTime pinnedTime;
}

