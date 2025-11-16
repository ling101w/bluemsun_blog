package com.bluemsun.blog.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新文章 DTO。
 */
@Data
public class ArticleUpdateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度过长")
    private String title;

    @Size(max = 512, message = "摘要过长")
    private String summary;

    @Size(max = 255, message = "封面链接过长")
    private String coverUrl;

    private Long categoryId;

    private String type;

    private BigDecimal price;

    private Boolean allowComment;

    @NotBlank(message = "正文不能为空")
    private String contentMd;

    private String contentHtml;

    private String attachments;

    /**
     * 是否发布，当为 false 时保存为草稿
     */
    private Boolean publish;
}
