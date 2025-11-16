package com.bluemsun.blog.module.article.vo;

import lombok.Data;

/**
 * 热门创作者推荐信息
 */
@Data
public class HotCreatorVO {

    private Long id;
    private String nickname;
    private String avatar;
    private Integer articleCount;
    private Long viewCount;
    private Long likeCount;
}
