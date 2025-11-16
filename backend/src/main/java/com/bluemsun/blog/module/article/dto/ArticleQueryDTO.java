package com.bluemsun.blog.module.article.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 文章分页查询参数。
 */
@Data
public class ArticleQueryDTO {

    @Min(1)
    private int pageNo = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 10;

    private String keyword;

    private Long categoryId;

    private String type;

    private String status;
}

