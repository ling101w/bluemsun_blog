package com.bluemsun.blog.module.article.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文章详情展示对象。
 */
@Data
public class ArticleDetailVO {

    private Long id;
    private Long authorId;
    private String title;
    private String summary;
    private String coverUrl;
    private Long categoryId;
    private String type;
    private BigDecimal price;
    private String status;
    private Boolean allowComment;
    private LocalDateTime publishTime;
    private String contentMd;
    private String contentHtml;
    private String attachments;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private boolean locked;
    private boolean purchased;
    private boolean canManageAttachments;
    private boolean owner;
    private Boolean pinned;
    private LocalDateTime pinnedTime;
}

