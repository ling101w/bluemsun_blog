package com.bluemsun.blog.module.article.service.model;

public class AttachmentDownload {

    private final byte[] data;
    private final String fileName;
    private final String contentType;

    public AttachmentDownload(byte[] data, String fileName, String contentType) {
        this.data = data;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }
}


