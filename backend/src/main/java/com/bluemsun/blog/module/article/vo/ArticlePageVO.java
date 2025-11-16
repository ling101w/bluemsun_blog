package com.bluemsun.blog.module.article.vo;

import java.util.List;

/**
 * 文章分页结果。
 */
public class ArticlePageVO {

    private long total;
    private int pageNo;
    private int pageSize;
    private List<ArticleListItemVO> records;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<ArticleListItemVO> getRecords() {
        return records;
    }

    public void setRecords(List<ArticleListItemVO> records) {
        this.records = records;
    }
}

