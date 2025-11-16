package com.bluemsun.blog.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 文件存储基础目录。
     */
    private String basePath = "./storage/uploads";

    /**
     * 单个文件最大大小（字节）。默认 20MB。
     */
    private long maxSize = 20 * 1024 * 1024;

    /**
     * 允许的文件扩展名。
     */
    private List<String> allowedExtensions = new ArrayList<>(List.of(
            "png", "jpg", "jpeg", "gif", "webp",
            "pdf", "doc", "docx", "ppt", "pptx",
            "xls", "xlsx", "txt", "md", "zip"
    ));

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}


