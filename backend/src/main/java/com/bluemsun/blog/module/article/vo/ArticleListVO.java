package com.bluemsun.blog.module.article.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文章列表项。
 */
@Data
public class ArticleListVO {

    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String type;
    private BigDecimal price;
    private String status;
    private LocalDateTime publishTime;
    private Long viewCount;
    private Long likeCount;
    private Boolean pinned;
    private LocalDateTime pinnedTime;
}

