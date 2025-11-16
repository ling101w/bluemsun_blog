package com.bluemsun.blog.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新建文章 DTO。
 */
@Data
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度过长")
    private String title;

    @Size(max = 512, message = "摘要过长")
    private String summary;

    @Size(max = 255, message = "封面链接过长")
    private String coverUrl;

    private Long categoryId;

    @NotBlank(message = "文章类型不能为空")
    private String type;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private Boolean allowComment;

    @NotBlank(message = "正文不能为空")
    private String contentMd;

    private String contentHtml;

    private String attachments;

    /**
     * 是否直接发布，默认保存为草稿
     */
    private Boolean publish;
}
