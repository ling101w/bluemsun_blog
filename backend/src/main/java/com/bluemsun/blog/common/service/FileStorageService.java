package com.bluemsun.blog.common.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.bluemsun.blog.common.config.OssProperties;
import com.bluemsun.blog.common.config.StorageProperties;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.exception.BizException;
import com.bluemsun.blog.common.web.FileResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

/**
 * OSS 文件统一管理：上传、校验、URL 构建与下载。
 */
@Service
public class FileStorageService {

    private final StorageProperties storageProperties;
    private final OssProperties ossProperties;
    private final OSS ossClient;

    public FileStorageService(StorageProperties storageProperties,
                              OssProperties ossProperties,
                              OSS ossClient) {
        this.storageProperties = storageProperties;
        this.ossProperties = ossProperties;
        this.ossClient = ossClient;
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ApiCode.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > storageProperties.getMaxSize()) {
            long limitMb = storageProperties.getMaxSize() / 1024 / 1024;
            throw new BizException(ApiCode.BAD_REQUEST, "文件过大，限制 " + limitMb + "MB");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (StringUtils.isBlank(extension)) {
            throw new BizException(ApiCode.BAD_REQUEST, "无法识别文件类型");
        }
        String lowerExt = extension.toLowerCase(Locale.ROOT);
        if (!storageProperties.getAllowedExtensions().contains(lowerExt)) {
            throw new BizException(ApiCode.BAD_REQUEST,
                    "不支持的文件类型，支持的类型：" + String.join("、", storageProperties.getAllowedExtensions()));
        }
    }

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ApiCode.BAD_REQUEST, "图片文件不能为空");
        }
        if (file.getSize() > storageProperties.getMaxSize()) {
            long limitMb = storageProperties.getMaxSize() / 1024 / 1024;
            throw new BizException(ApiCode.BAD_REQUEST, "图片文件过大，限制 " + limitMb + "MB");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (StringUtils.isBlank(extension)) {
            throw new BizException(ApiCode.BAD_REQUEST, "无法识别文件类型");
        }
        String[] imageExtensions = {"png", "jpg", "jpeg", "gif", "webp"};
        String lowerExt = extension.toLowerCase(Locale.ROOT);
        boolean allowed = false;
        for (String ext : imageExtensions) {
            if (ext.equals(lowerExt)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new BizException(ApiCode.BAD_REQUEST, "仅支持上传图片格式：" + String.join("、", imageExtensions));
        }
    }

    public String extractExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) {
            return "";
        }
        return fileName.substring(idx + 1);
    }

    public String generateFileName(String originalFileName) {
        String extension = extractExtension(originalFileName);
        return UUID.randomUUID() + (StringUtils.isNotBlank(extension) ? "." + extension : "");
    }

    public String uploadToOss(MultipartFile file, String key) {
        try (InputStream in = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            if (StringUtils.isNotBlank(file.getContentType())) {
                meta.setContentType(file.getContentType());
            }
            meta.setContentLength(file.getSize());
            ossClient.putObject(ossProperties.getBucket(), key, in, meta);
            return key;
        } catch (Exception e) {
            throw new BizException(ApiCode.SERVER_ERROR, "文件上传失败：" + e.getMessage());
        }
    }

    public String buildPublicUrl(String key) {
        if (StringUtils.isNotBlank(ossProperties.getPublicDomain())) {
            String domain = ossProperties.getPublicDomain();
            String base = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
            return base + "/" + key;
        }
        return "https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint() + "/" + key;
    }

    public void deleteFromOss(String key) {
        try {
            ossClient.deleteObject(ossProperties.getBucket(), key);
        } catch (Exception ignored) {
        }
    }

    public FileResource loadFile(String key, String downloadName) {
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(ossProperties.getBucket(), key);
            OSSObject object = ossClient.getObject(ossProperties.getBucket(), key);
            String fileName = StringUtils.isNotBlank(downloadName) ? downloadName : key;
            String contentType = StringUtils.defaultIfBlank(metadata.getContentType(), "application/octet-stream");
            long length = metadata.getContentLength();
            return new FileResource(object.getObjectContent(), fileName, contentType, length);
        } catch (Exception e) {
            throw new BizException(ApiCode.NOT_FOUND, "文件不存在或已被删除");
        }
    }
}
