package com.bluemsun.blog.module.article.service.model;

public class AttachmentSummary {

    private Long id;
    private String name;
    private String url;
    private boolean restrict;
    private Long size;

    public AttachmentSummary() {
    }

    public AttachmentSummary(Long id, String name, String url, boolean restrict, Long size) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.restrict = restrict;
        this.size = size;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRestrict() {
        return restrict;
    }

    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}


